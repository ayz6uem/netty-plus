package com.ybyc.gateway.nettyplus.core.codec;

import com.ybyc.gateway.nettyplus.core.TcpServer;
import com.ybyc.gateway.nettyplus.core.option.Option;
import com.ybyc.gateway.nettyplus.core.option.StringOption;
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
import java.util.Objects;

/**
 * 类编码器，将类对象编码为ByteBuf，支持基本类型和String
 * @author wangzhe
 */
public class ObjectEncoder {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private Object data;
    private ByteOrder byteOrder = TcpServer.Options.DEFAULT_BYTEORDER;
    private ByteBuf byteBuf;

    public ObjectEncoder(Object data) {
        this.data = data;
        byteBuf = Unpooled.buffer();
    }

    public ObjectEncoder(Object data, ByteBuf byteBuf) {
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
                    encode(field,value);
                }
            }
        } catch (Exception e) {
            logger.error("encode {} failed, cause:{}", data.getClass().getSimpleName(), e.getMessage(), e);
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
            new ObjectEncoder(value, byteBuf).encode();
            return true;
        } catch (Exception e) {
            throw new EncoderException(e);
        }
    }

    private boolean encodePrimitive(Number value,Option option) throws Exception {
        int bytes = option!=null?option.value():ReflectHelper.primitiveBytes(value.getClass());
        ByteBufHelper.write(byteBuf, bytes, value, byteOrder);
        return true;
    }

    private boolean encodeString(String value,Option option){
        byte[] bytes = OptionHelper.convertToBytes(value,option.string()!=null?option.string(): StringOption.NATURAL);
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
                    new ObjectEncoder(coll, byteBuf).encode();
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
                new ObjectEncoder(object, byteBuf).encode();
            }
        }
        return true;
    }

}
