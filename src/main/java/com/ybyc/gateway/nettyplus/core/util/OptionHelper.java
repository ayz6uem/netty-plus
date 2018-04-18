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
        return result;
    }

    public static byte[] convertToBytes(String value, StringOption option) {
        switch (option){
            case HEX:
                if(value.length()%2==1){
                    value = "0"+value;
                }
                return ByteBufUtil.decodeHexDump(value);
            case BINARY:
                return ByteBufHelper.decodeBinaryDump(value);
            default:
                return value.getBytes();
        }
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

    public static boolean containGeneric(Object target, Class<?> targetClass){

        Collection<Field> collection = ReflectHelper.getDataField(target.getClass());
        Iterator<Field> fields = collection.iterator();
        try {
            while (fields.hasNext()){
                Field field = fields.next();
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

//    public static boolean containGeneric(Object target, Class<?> targetClass){
//
//        class BooleanHolder{
//            public boolean value;
//        }
//        final BooleanHolder holder = new BooleanHolder();
//
//        DoWithField.of(target).with((field,value)->{
//            if(Object.class.equals(field.getType())){
//                try {
//                    Option option = field.getAnnotation(Option.class);
//                    Class<?> componentClass = getFieldActualClass(target,field,option);
//                    if(Objects.equals(componentClass,targetClass)){
//                        holder.value = true;
//                        return false;
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//            return true;
//        });
//
//        return holder.value;
//    }
//
//    public static class DoWithField {
//
//        Object data;
//
//        public static DoWithField of(Object data) {
//            DoWithField consumer = new DoWithField();
//            consumer.data=data;
//            return consumer;
//        }
//
//        public void with(BiFunction<Field,Object,Boolean> classBiConsumer){
//            with(data.getClass(),classBiConsumer);
//        }
//
//        public boolean with(Class<?> clzz, BiFunction<Field,Object,Boolean> classBiConsumer){
//            if(Object.class.equals(clzz)){
//                return true;
//            }
//            boolean continued = with(clzz.getSuperclass(),classBiConsumer);
//
//            if(continued){
//                Field[] fields = clzz.getDeclaredFields();
//                for (int i = 0; i < fields.length; i++) {
//                    Field field = fields[i];
//                    continued = classBiConsumer.apply(field,clzz);
//                    if(!continued){
//                        return continued;
//                    }
//                }
//
//            }
//
//            return continued;
//
//        }
//
//    }

}
