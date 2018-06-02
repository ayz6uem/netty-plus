package com.ybyc.gateway.nettyplus.core.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * K-L-V key，length，value
 * BeanDecoder遇到@KeyLength时，读取key，再读取value，获取Key字段
 * 应标注在klv项数字段上
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Groups {

    /**
     * klv组数，默认使用字段值
     * @return
     */
    int value() default -1;

    /**
     * key字段字节数
     * @return
     */
    int keyBytes() default 1;

    /**
     * length字段字节数
     * @return
     */
    int lengthBytes() default 1;
}
