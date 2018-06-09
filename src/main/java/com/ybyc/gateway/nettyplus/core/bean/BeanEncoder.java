package com.ybyc.gateway.nettyplus.core.bean;

import com.ybyc.gateway.nettyplus.core.TcpServer;
import com.ybyc.gateway.nettyplus.core.util.Assert;
import com.ybyc.gateway.nettyplus.core.util.ByteBufHelper;
import com.ybyc.gateway.nettyplus.core.util.OptionHelper;
import com.ybyc.gateway.nettyplus.core.util.ReflectHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * 类编码器，将类对象编码为ByteBuf，支持基本类型和String
 * @author wangzhe
 */
public class BeanEncoder {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private Object data;
    private ByteBuf byteBuf;

    private ByteOrder byteOrder = TcpServer.Options.DEFAULT_BYTEORDER;

    public BeanEncoder byteOrder(ByteOrder byteOrder){
        this.byteOrder = byteOrder;
        return this;
    }

    public BeanEncoder(Object data) {
        this.data = data;
        byteBuf = Unpooled.buffer();
    }

    public BeanEncoder(Object data, ByteBuf byteBuf) {
        this.data = data;
        this.byteBuf = byteBuf;
    }

    public ByteBuf encode() {
        Assert.notNull(data, "can not encode null");
        Iterator<Field> fields = ReflectHelper.getDataField(data.getClass()).iterator();
        try {
            while (fields.hasNext()){
                Field field = fields.next();
                Object value = field.get(data);
                if (value != null) {
                    if(OptionHelper.idGroups(field)){
                        int startBytes = byteBuf.readableBytes();
                        encode(field,value);
                        encodeGroups(data,field,startBytes);
                    }else{
                        encode(field,value);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("encode {} failed, cause:{}", data.getClass().getName(), e.getMessage(), e);
            throw new EncoderException(e);
        }
        return byteBuf;
    }

    private boolean encode(Field field, Object value) {
        try {
            Option option = field.getAnnotation(Option.class);
            Class<?> fieldClass = value.getClass();

            if (ReflectHelper.isPrimitive(fieldClass)) {
                return encodePrimitive((Number) value,option);
            }
            if (Objects.equals(String.class, value.getClass())) {
                return encodeString((String)value,option);
            }
            if (Collection.class.isAssignableFrom(value.getClass())) {
                return encodeCollection((Collection) value);
            }
            if (value.getClass().isArray()) {
                return encodeArray(value);
            }
            new BeanEncoder(value, byteBuf).encode();
            return true;
        } catch (Exception e) {
            throw new EncoderException(e);
        }
    }

    /**
     * 编码klv
     * @param data
     * @param field
     * @param groupsStartBytes
     * @throws IllegalAccessException
     */
    private void encodeGroups(Object data, Field field, int groupsStartBytes) throws IllegalAccessException {
        Groups groupsAnno = field.getAnnotation(Groups.class);
        Map<Object,Field> keyFieldMap = ReflectHelper.getKeyField(data.getClass());
        int groupsLength = byteBuf.readableBytes() - groupsStartBytes;
        int groups = 0;
        int keyBytes = groupsAnno.keyBytes();
        int lengthBytes = groupsAnno.lengthBytes();

        /**
         * 遍历所有@Key的字段
         */
        Iterator<Field> iterator = keyFieldMap.values().iterator();
        while (iterator.hasNext()){
            Field keyField = iterator.next();
            keyField.setAccessible(true);
            Object value = keyField.get(data);
            if(value==null){
                //忽略null值
                continue;
            }
            groups++;

            Key key = keyField.getAnnotation(Key.class);

            //写 key
            ByteBufHelper.write(byteBuf, keyBytes, key.value(), byteOrder);
            //写 length 用 0填充
            ByteBufHelper.write(byteBuf, lengthBytes, 0, byteOrder);

            int valueStartBytes = byteBuf.readableBytes();
            encode(keyField,value);
            int length = byteBuf.readableBytes() - valueStartBytes;

            //回填length的值
            ByteBufHelper.set(byteBuf, valueStartBytes-lengthBytes,lengthBytes, byteOrder, length);

        }

        //自动填入groups的值
        ByteBufHelper.set(byteBuf,groupsStartBytes,groupsLength,byteOrder,groups);

    }

    private boolean encodePrimitive(Number value,Option option) throws Exception {
        int bytes = option!=null?option.value():ReflectHelper.primitiveBytes(value.getClass());
        ByteBufHelper.write(byteBuf, bytes, value, byteOrder);
        return true;
    }

    private boolean encodeString(String value,Option option){
        int length = 0;
        StringOption stringOption = StringOption.NATURAL;
        if(Objects.nonNull(option)){
            length = option.value();
            stringOption = option.string();
        }
        byte[] bytes = OptionHelper.convertToBytes(value, length, stringOption);
        byteBuf.writeBytes(bytes);
        return true;
    }

    private boolean encodeCollection(Collection collection){
        collection.stream().forEach(coll -> {
            try {
                if (ReflectHelper.isPrimitive(coll.getClass())) {
                    int bytes = ReflectHelper.primitiveBytes(coll.getClass());
                    ByteBufHelper.write(byteBuf, bytes, (Number) coll, byteOrder);
                } else {
                    new BeanEncoder(coll, byteBuf).encode();
                }
            } catch (Exception e) {
                throw new EncoderException("can not encoder collection");
            }

        });
        return true;
    }

    private boolean encodeArray(Object value) throws Exception{
        int length = Array.getLength(value);
        for (int i = 0; i < length; i++) {
            Object object = Array.get(value, i);
            if (ReflectHelper.isPrimitive(object.getClass())) {
                int bytes = ReflectHelper.primitiveBytes(object.getClass());
                ByteBufHelper.write(byteBuf, bytes, (Number) object, byteOrder);
            } else {
                new BeanEncoder(object, byteBuf).encode();
            }
        }
        return true;
    }

}
