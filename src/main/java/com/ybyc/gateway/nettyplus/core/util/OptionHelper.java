package com.ybyc.gateway.nettyplus.core.util;


import com.ybyc.gateway.nettyplus.core.bean.ClassFieldContext;
import com.ybyc.gateway.nettyplus.core.bean.Groups;
import com.ybyc.gateway.nettyplus.core.bean.Option;
import com.ybyc.gateway.nettyplus.core.bean.StringOption;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Option处理工具
 * @author wangzhe
 */
public class OptionHelper {

    public static int getLength(Option option, Object data){
        if(option == null){
            return -1;
        }
        int length = option.value();
        if (!"".equals(option.lengthField())) {
            length = ReflectHelper.getIntValue(data, option.lengthField());
        }
        return length;
    }

    public static boolean idGroups(Field field){
        return field.getAnnotation(Groups.class) != null;
    }

    public static String convertToString(ByteBuf byteBuf, StringOption option, int length) {
        String result;
        ByteBuf stringByteBuf = byteBuf.readBytes(length);
        switch (option){
            case HEX:
                result =  ByteBufUtil.hexDump(stringByteBuf).toUpperCase();
                break;
            case BINARY:
                result =  ByteBufHelper.binaryDump(stringByteBuf);
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

        Collection<Field> collection = ClassFieldContext.getDataField(target.getClass());
        Iterator<Field> fields = collection.iterator();
        try {
            while (fields.hasNext()){
                Field field = fields.next();
                Object value = field.get(target);
                if(Objects.nonNull(value) && Objects.equals(value.getClass(),targetClass)){
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public static <T> int getGroupSize(Groups groups, T template, Field field) {
        if(groups.value()!=-1){
            return groups.value();
        }
        try {
            field.setAccessible(true);
            return ((Number) field.get(template)).intValue();
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
