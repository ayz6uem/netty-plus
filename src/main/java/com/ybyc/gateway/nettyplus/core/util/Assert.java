package com.ybyc.gateway.nettyplus.core.util;

import java.util.Collection;
import java.util.Objects;

/**
 * @author wangzhe
 */
public class Assert {

    public static void notNull(Object object, String msg){
        if(Objects.isNull(object)){
            throw new IllegalArgumentException(msg);
        }
    }

    public static void notString(Class<?> clzz, String msg){
        if(String.class.equals(clzz)){
            throw new IllegalArgumentException(msg);
        }
    }

    public static void notCollection(Class<?> clzz, String msg){
        if(Collection.class.isAssignableFrom(clzz)){
            throw new IllegalArgumentException(msg);
        }
    }

    public static void notArray(Class<?> clzz, String msg){
        if(clzz.isArray()){
            throw new IllegalArgumentException(msg);
        }
    }

}
