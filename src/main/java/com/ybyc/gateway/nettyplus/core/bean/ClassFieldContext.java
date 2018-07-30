package com.ybyc.gateway.nettyplus.core.bean;

import com.ybyc.gateway.nettyplus.core.util.BodyTailCollection;
import com.ybyc.gateway.nettyplus.core.util.ReflectHelper;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 字段上下文
 */
public class ClassFieldContext {

    private static final Map<Class<?>,Collection<Field>> fieldPool = new HashMap<>();

    /**
     * 从池中获取字段缓存
     * @param clzz
     * @return
     */
    public static Collection<Field> getDataField(Class<?> clzz){
        Collection<Field> fields = fieldPool.get(clzz);
        if(fields==null){
            BodyTailCollection<Field> bodyTailCollection = findDataField(clzz);
            fields = bodyTailCollection.immutable();
            fieldPool.put(clzz,fields);
        }
        return fields;
    }

    private static BodyTailCollection<Field> findDataField(Class<?> clzz){
        if(Object.class.equals(clzz)){
            return new BodyTailCollection<>();
        }
        BodyTailCollection<Field> collection = findDataField(clzz.getSuperclass());
        Field[] array = clzz.getDeclaredFields();
        for(Field field : array){
            if(!isDataField(field,clzz)){
                continue;
            }
            field.setAccessible(true);
            if(ReflectHelper.hasAnnotation(field,TailField.class)){
                collection.getTail().add(field);
            }else{
                collection.add(field);
            }
        }
        return collection;
    }


    private static Map<Class<?>,Map<Object,Field>> keyFieldPool = new HashMap<>();

    public static Map<Object, Field> getKeyField(Class<?> clzz) {
        Map<Object,Field> keyFields = keyFieldPool.get(clzz);
        if(keyFields==null){
            keyFields = findKeyField(clzz);
            keyFieldPool.put(clzz,keyFields);
        }
        return keyFields;
    }

    private static Map<Object, Field> findKeyField(Class<?> clzz) {
        if(Object.class.equals(clzz)){
            return new HashMap<>();
        }
        Map<Object,Field> fields = findKeyField(clzz.getSuperclass());
        Field[] array = clzz.getDeclaredFields();
        for(Field field : array){
            if(!isKeyField(field,clzz)){
                continue;
            }
            field.setAccessible(true);
            Key key = field.getAnnotation(Key.class);
            fields.put(key.value(),field);
        }
        return fields;
    }


    public static boolean isDataField(Field field,Class<?> clzz){
        if(field.isSynthetic()){
            return false;
        }
        if(Modifier.isStatic(field.getModifiers())){
            return false;
        }
        if((field.getAnnotation(Exclude.class))!=null){
            return false;
        }
        if((field.getAnnotation(Key.class))!=null){
            return false;
        }
        return hasGetSet(field, clzz);
    }


    public static boolean isKeyField(Field field,Class<?> clzz){
        if(field.isSynthetic()){
            return false;
        }
        if(Modifier.isStatic(field.getModifiers())){
            return false;
        }
        if((field.getAnnotation(Key.class))==null){
            return false;
        }
        return hasGetSet(field, clzz);
    }

    private static boolean hasGetSet(Field field,Class<?> clzz){
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
