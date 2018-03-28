package com.ybyc.gateway.nettyplus.core.codec;

import com.ybyc.gateway.nettyplus.core.TcpServer;
import com.ybyc.gateway.nettyplus.core.option.Option;
import com.ybyc.gateway.nettyplus.core.util.Assert;
import com.ybyc.gateway.nettyplus.core.util.ByteBufHelper;
import com.ybyc.gateway.nettyplus.core.util.OptionHelper;
import com.ybyc.gateway.nettyplus.core.util.ReflectHelper;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Iterator;
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
public class ObjectDecoder {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private ByteBuf byteBuf;

    private ByteOrder byteOrder = TcpServer.Options.DEFAULT_BYTEORDER;

    public ObjectDecoder(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    public ObjectDecoder byteOrder(ByteOrder byteOrder){
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
                Object value = decode(template, field);
                field.set(template, value);
            }
        } catch (Exception e) {
            logger.error("decode {} failed, cause:{}", template.getClass().getName(), e.getMessage(), e);
            throw new DecoderException(e);
        }
        return template;
    }

    private <T> Object decode(T template, Field field) throws Exception {
        Option option = field.getAnnotation(Option.class);
        Class<?> fieldClass = OptionHelper.getFieldActualClass(template, field, option);

        if (ReflectHelper.isPrimitive(fieldClass)) {
            return decodePrimitive(fieldClass, option);
        }
        if (Objects.equals(String.class, fieldClass)) {
            return decodeString(template, option);
        }
        if (fieldClass.isArray()) {
            return decodeArray(template, fieldClass, option);
        }
        if (Collection.class.isAssignableFrom(fieldClass)) {
            return decodeCollection(template, fieldClass, option);
        }

        return new ObjectDecoder(byteBuf).decode(fieldClass.newInstance());

    }

    private Object decodePrimitive(Class<?> fieldClass, Option option) throws Exception {
        return ByteBufHelper.readPrimitive(byteBuf
                , option != null ? option.value() : ReflectHelper.primitiveBytes(fieldClass)
                , byteOrder
                , option != null && option.unsigned());
    }

    private String decodeString(Object template, Option option) {
        Assert.notNull(option, "unknow String's length and decode type, please use @Option");
        int length = OptionHelper.getLength(template, option);
        return OptionHelper.convertToString(byteBuf, option.string(), length);
    }

    private Object decodeArray(Object template, Class<?> fieldClass, Option option) throws Exception {
        Assert.notNull(option, "unknow Array's length, can not decode, please use @Option");
        Class<?> componentClass = fieldClass.getComponentType();
        Assert.notString(componentClass, "please use wrapper class on String, because i need @Option on field");
        Assert.notCollection(componentClass, "don't support Collection[] now");
        Assert.notArray(componentClass, "don't support Object[][] now");

        int length = OptionHelper.getLength(template, option);
        Object array = Array.newInstance(componentClass, length);
        for (int i = 0; i < length; i++) {
            Object value;
            if (ReflectHelper.isPrimitive(componentClass)) {
                int bytes = ReflectHelper.primitiveBytes(componentClass);
                value = ByteBufHelper.readPrimitive(byteBuf, bytes, byteOrder, false);
            } else {
                value = new ObjectDecoder(byteBuf).decode(componentClass.newInstance());
            }
            Array.set(array, i, value);
        }

        return array;
    }

    private Collection decodeCollection(Object template, Class<?> fieldClass, Option option) throws Exception {
        Assert.notNull(option, "collection without @Option,unknow gemericClass");
        Class<?> componentClass = OptionHelper.getActualClass(template, option);
        Assert.notString(componentClass, "please use wrapper class on String, because i need @Option on field");
        Assert.notCollection(componentClass, "don't support Collection[] now");
        Assert.notArray(componentClass, "don't support Object[][] now");

        Collection collection = ReflectHelper.newCollection(fieldClass);
        int length = OptionHelper.getLength(template, option);
        int index = 0;
        while (length == -1 || (index++) < length) {
            if (ReflectHelper.isPrimitive(componentClass)) {
                int bytes = ReflectHelper.primitiveBytes(componentClass);
                collection.add(ByteBufHelper.readPrimitive(byteBuf, bytes, byteOrder, false));
            } else {
                collection.add(new ObjectDecoder(byteBuf).decode(componentClass.newInstance()));
            }
        }
        return collection;
    }

}
