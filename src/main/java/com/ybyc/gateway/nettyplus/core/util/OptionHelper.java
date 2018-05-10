package com.ybyc.gateway.nettyplus.core.util;


import com.ybyc.gateway.nettyplus.core.option.Option;
import com.ybyc.gateway.nettyplus.core.option.StringOption;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Option处理工具
 * @author wangzhe
 */
public class OptionHelper {

    public static int getLength(Object data, Option option){
        int length = option.value();
        if (!"".equals(option.lengthField())) {
            length = ReflectHelper.getIntValue(data, option.lengthField());
        }
        return length;
    }



    public static String convertToString(ByteBuf byteBuf, StringOption option, int length) {
        String result;
        ByteBuf stringByteBuf = byteBuf.readBytes(length);
        switch (option){
            case HEX:
                result =  ByteBufUtil.hexDump(stringByteBuf).toUpperCase();
                break;
            case BINARY:
                result =  ByteBufHelper.binaryDump(byteBuf.readBytes(length));
                break;
            default:
                result = new String(ByteBufUtil.getBytes(stringByteBuf));
        }
        ReferenceCountUtil.release(stringByteBuf);
        return result.trim();
    }

    public static byte[] convertToBytes(String value, int length, StringOption option) {
        byte[] result;
        switch (option){
            case HEX:
                if(value.length()%2==1){
                    value = "0"+value;
                }
                result = ByteBufUtil.decodeHexDump(value);
                break;
            case BINARY:
                result = ByteBufHelper.decodeBinaryDump(value);
                break;
            default:
                result = value.getBytes();
        }
        if(length!=0){
            result = ByteBufHelper.fillBytes(result,length);
        }
        return result;
    }

    public static <T> Class<?> getActualClass(T template, Option option) throws ClassNotFoundException {
        if (!Object.class.equals(option.genericClass())) {
            return option.genericClass();
        } else {
            return ReflectHelper.getActualClass(template, option.generic());
        }
    }

    public static <T> Class<?> getFieldActualClass(T template, Field field, Option option) throws ClassNotFoundException {
        Class<?> componentClass = Object.class;
        String componentGeneric = "T";
        if(option!=null){
            componentClass = option.genericClass();
            componentGeneric = option.generic();
        }
        if(!Object.class.equals(field.getType())){
            return field.getType();
        }
        if(!Object.class.equals(componentClass)){
            return componentClass;
        }else{
            return ReflectHelper.getActualClass(template,componentGeneric);
        }
    }

    public static boolean containClass(Object target, Class<?> targetClass){

        Collection<Field> collection = ReflectHelper.getDataField(target.getClass());
        Iterator<Field> fields = collection.iterator();
        try {
            while (fields.hasNext()){
                Field field = fields.next();
                Object value = field.get(target);
                if(Objects.nonNull(value) && Objects.equals(value.getClass(),targetClass)){
                    return true;
                }
                if(Object.class.equals(field.getType())){
                    Option option = field.getAnnotation(Option.class);
                    Class<?> componentClass = getFieldActualClass(target,field,option);
                    if(Objects.equals(componentClass,targetClass)){
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

}
