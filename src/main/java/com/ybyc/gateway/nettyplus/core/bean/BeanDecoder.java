package com.ybyc.gateway.nettyplus.core.bean;

import com.ybyc.gateway.nettyplus.core.TcpServer;
import com.ybyc.gateway.nettyplus.core.util.Assert;
import com.ybyc.gateway.nettyplus.core.util.ByteBufHelper;
import com.ybyc.gateway.nettyplus.core.util.OptionHelper;
import com.ybyc.gateway.nettyplus.core.util.ReflectHelper;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * 类型解析器
 * <p>
 * 1 查找要decode 的属性
 * 2 判断属性的类型 基本类型 字符串 数组（基本类型 字符串（不支持） 扩展类） 集合 泛型类 扩展类
 * 3 不支持 bit读取，只支持byte读取；支持hexString 和binaryString的解析。
 *
 * @author wangzhe
 */
public class BeanDecoder {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private ByteBuf byteBuf;

    private ByteOrder byteOrder = TcpServer.Options.DEFAULT_BYTEORDER;

    public BeanDecoder(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    public BeanDecoder byteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
        return this;
    }

    public <T> T decode(T template) {
        Assert.notArray(template.getClass(), "please use ArrayDecoder to decode an array");
        Assert.notCollection(template.getClass(), "please use CollectionDecoder to decode an collection");
        try {
            Iterator<Field> fields = ReflectHelper.getDataField(template.getClass()).iterator();
            while (fields.hasNext()) {
                Field field = fields.next();
                decodeField(template, field);
                if(OptionHelper.idGroups(field)){
                    decodeGroups(template,field);
                }
            }
            return template;
        } catch (Exception e) {
            logger.error("decode {} failed, cause:{}", template.getClass().getName(), e.getMessage(), e);
            throw new DecoderException(e);
        }
    }

    private <T> void decodeField(T template, Field field) throws Exception {
        Object value = decode(template, field, -1);
        if (Objects.nonNull(value)) {
            Method setMethod = new PropertyDescriptor(field.getName(), template.getClass()).getWriteMethod();
            setMethod.invoke(template, value);
        }
    }

    private <T> void decodeGroups(T template, Field field) throws Exception {
        Groups groups = field.getAnnotation(Groups.class);
        int groupSize = OptionHelper.getGroupSize(groups,template,field);
        int keyBytes = groups.keyBytes();
        int lengthBytes = groups.lengthBytes();
        if(lengthBytes<Byte.BYTES || lengthBytes>Long.BYTES){
            throw new IllegalArgumentException("klv length bytes error:"+lengthBytes);
        }
        Map<Object,Field> keyFieldMap = ReflectHelper.getKeyField(template.getClass());
        for(int i=0;i<groupSize;i++){
            Number key = (Number)ByteBufHelper.readPrimitive(byteBuf,keyBytes,byteOrder,true);
            Number length = (Number)ByteBufHelper.readPrimitive(byteBuf,lengthBytes,byteOrder,true);
            Field keyField = keyFieldMap.get(key.longValue());
            Object value = decode(template,keyField,length.intValue());
            if (Objects.nonNull(value)) {
                Method setMethod = new PropertyDescriptor(keyField.getName(), template.getClass()).getWriteMethod();
                setMethod.invoke(template, value);
            }
        }
    }

    private <T> Object decode(T template, Field field, int readLength) throws Exception {
        Option option = field.getAnnotation(Option.class);
        try {
            Class<?> fieldClass = OptionHelper.getFieldActualClass(template, field, option);

            int length = readLength==-1?OptionHelper.getLength(option, template):readLength;

            if (ReflectHelper.isPrimitive(fieldClass)) {
                return decodePrimitive(fieldClass, length, option != null && option.unsigned());
            }
            if (Objects.equals(ByteBuf.class, fieldClass)) {
                return decodeBytes(length);
            }
            if (Objects.equals(String.class, fieldClass)) {
                return decodeString(length,option != null?option.string():StringOption.NATURAL);
            }
            if (fieldClass.isArray()) {
                return decodeArray(fieldClass, length);
            }
            if (Collection.class.isAssignableFrom(fieldClass)) {
                return decodeCollection(fieldClass, length, OptionHelper.getActualClass(template, option));
            }

            return new BeanDecoder(byteBuf).decode(fieldClass.newInstance());
        } catch (Exception e) {
            if (option != null && !option.required()) {
                return null;
            }
            throw e;
        }

    }

    private Object decodePrimitive(Class<?> fieldClass, int length, boolean unsigned) throws Exception {
        if(length==-1){
            length = ReflectHelper.primitiveBytes(fieldClass);
        }
        return ByteBufHelper.readPrimitive(byteBuf, length, byteOrder, unsigned);
    }

    private ByteBuf decodeBytes(int length) {
        Assert.notEquals(length, -1, "unknow bytebuf's length and decode type, please use @Option");
        return byteBuf.readBytes(length);
    }

    private String decodeString(int length, StringOption stringOption) {
        Assert.notEquals(length,-1, "unknow String's length and decode type, please use @Option");
        return OptionHelper.convertToString(byteBuf, stringOption, length);
    }

    private Object decodeArray(Class<?> fieldClass, int length) throws Exception {
        Assert.notEquals(length, -1, "unknow Array's length, can not decode, please use @Option");
        Class<?> componentClass = fieldClass.getComponentType();
        Assert.notString(componentClass, "please use wrapper class on String, because i need @Option on field");
        Assert.notCollection(componentClass, "don't support Collection[] now");
        Assert.notArray(componentClass, "don't support Object[][] now");

        Object array = Array.newInstance(componentClass, length);
        for (int i = 0; i < length; i++) {
            Object value;
            if (ReflectHelper.isPrimitive(componentClass)) {
                int bytes = ReflectHelper.primitiveBytes(componentClass);
                value = ByteBufHelper.readPrimitive(byteBuf, bytes, byteOrder, false);
            } else {
                value = new BeanDecoder(byteBuf).decode(componentClass.newInstance());
            }
            Array.set(array, i, value);
        }

        return array;
    }

    private Collection decodeCollection(Class<?> fieldClass, int length, Class<?> componentClass) throws Exception {
        Assert.notEquals(length, -1, "collection without @Option,unknow gemericClass");
        Assert.notString(componentClass, "please use wrapper class on String, because i need @Option on field");
        Assert.notCollection(componentClass, "don't support Collection[] now");
        Assert.notArray(componentClass, "don't support Object[][] now");

        Collection collection = ReflectHelper.newCollection(fieldClass);
        int index = 0;
        while (length == -1 || (index++) < length) {
            if (ReflectHelper.isPrimitive(componentClass)) {
                int bytes = ReflectHelper.primitiveBytes(componentClass);
                collection.add(ByteBufHelper.readPrimitive(byteBuf, bytes, byteOrder, false));
            } else {
                collection.add(new BeanDecoder(byteBuf).decode(componentClass.newInstance()));
            }
        }
        return collection;
    }

}
