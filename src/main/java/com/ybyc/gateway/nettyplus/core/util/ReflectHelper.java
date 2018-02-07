package com.ybyc.gateway.nettyplus.core.util;

import io.netty.handler.codec.DecoderException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射助手类
 *
 * @author wangzhe
 */
public class ReflectHelper {

    private static Map<Class<?>,List<Field>> fieldPool = new ConcurrentHashMap<>();

    public static Collection<Field> getDataField(Class<?> clzz){
        Collection<Field> fields = fieldPool.get(clzz);
        if(fields==null){
            fields = findDataField(clzz);
        }
        return fields;
    }

    private static Collection<Field> findDataField(Class<?> clzz){
        if(Object.class.equals(clzz)){
            return new ArrayList<>();
        }
        Collection<Field> fields = findDataField(clzz.getSuperclass());
        Field[] array = clzz.getDeclaredFields();
        for(Field field : array){
            if(!ReflectHelper.isDataField(field,clzz)){
                continue;
            }
            field.setAccessible(true);
            fields.add(field);
        }
        return fields;
    }



    public static boolean hasAnnotation(Field field, Class<? extends Annotation> clzz) {
        return field.getAnnotation(clzz) != null;
    }

    /**
     * 是否是基本数据类型
     *
     * @param clzz
     * @return
     */
    public static boolean isPrimitive(Class clzz) {
        return clzz.isPrimitive() || Number.class.isAssignableFrom(clzz);
    }

    /**
     * 获取基本类型的包装类
     *
     * @param clzz
     * @return
     */
    public static Class<?> getWrapperClass(Class<?> clzz) {
        if (Objects.equals(byte.class.getName(), clzz.getName())) {
            return Byte.class;
        }
        if (Objects.equals(short.class.getName(), clzz.getName())) {
            return Short.class;
        }
        if (Objects.equals(int.class.getName(), clzz.getName())) {
            return Integer.class;
        }
        if (Objects.equals(long.class.getName(), clzz.getName())) {
            return Long.class;
        }
        if (Objects.equals(float.class.getName(), clzz.getName())) {
            return Float.class;
        }
        if (Objects.equals(double.class.getName(), clzz.getName())) {
            return Double.class;
        }
        if (Objects.equals(char.class.getName(), clzz.getName())) {
            return Character.class;
        }
        if (Objects.equals(boolean.class.getName(), clzz.getName())) {
            return Boolean.class;
        }
        throw new DecoderException(clzz.getSimpleName() + " is not a primitive class");
    }

    /**
     * 获取字段的字节数
     *
     * @param clazz
     * @return
     * @throws Exception
     */
    public static int primitiveBytes(Class clazz) throws Exception {
        Class<?> fieldType = clazz;
        if (fieldType.isPrimitive()) {
            fieldType = getWrapperClass(fieldType);
        }
        if (Number.class.isAssignableFrom(fieldType)) {
            return (int) fieldType.getDeclaredField("BYTES").get(fieldType);
        }
        if (Character.class.equals(fieldType)){
            throw new IllegalAccessException("please use bytes[] instead of char");
        }
        if (Boolean.class.equals(fieldType)){
            throw new IllegalAccessException("please use byte instead of boolean");
        }
        throw new IllegalAccessException("no annotation needed, needs Bytes or Bits");
    }

    public static <T> int getIntValue(T template, String fieldName) {
        try {
            Field field = template.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return ((Number) field.get(template)).intValue();
        } catch (Exception e) {
            throw new DecoderException(e.getMessage());
        }
    }

    public static <T> Collection<T> newCollection(Class<?> clzz){
        if (List.class.isAssignableFrom(clzz)) {
            return new ArrayList<>();
        }else{
            return new HashSet<>();
        }
    }

    /**
     * 获取泛型的真实类型
     *
     * @param message
     * @param genericType
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> getActualClass(Object message, String genericType) throws ClassNotFoundException {

        if (Object.class.equals(message.getClass().getSuperclass())) {
            throw new DecoderException("can not decode Object; or please give me a new Entity<" + genericType +
                    ">(){}, then i will get " + genericType + ".class");
        }

        int typeParamIndex = -1;
        TypeVariable<?>[] typeParams = message.getClass().getSuperclass().getTypeParameters();
        for (int i = 0; i < typeParams.length; ++i) {
            if (Objects.equals(genericType, typeParams[i].getName())) {
                typeParamIndex = i;
                break;
            }
        }
        if (typeParamIndex != -1) {
            Type genericSuperClass = message.getClass().getGenericSuperclass();
            if (genericSuperClass instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperClass;
                Type type = parameterizedType.getActualTypeArguments()[typeParamIndex];
                return Class.forName(type.getTypeName());
            }
        }
        throw new DecoderException(" Object can not decode, if it is a T, use @Generic and new Entity<T>(){}, then i will get T.class");
    }

    public static boolean hasGeneric(Class<?> clzz){
        TypeVariable<?>[] typeParams = clzz.getSuperclass().getTypeParameters();
        if(typeParams!=null&&typeParams.length>0){
            return true;
        }
        return false;
    }

    public static boolean isDataField(Field field,Class<?> clzz){
        if(field.isSynthetic()){
            return false;
        }
        if(Modifier.isStatic(field.getModifiers())){
            return false;
        }
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(),clzz);
            if(descriptor.getReadMethod()!=null&&descriptor.getWriteMethod()!=null){
                return true;
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return false;
    }

}
