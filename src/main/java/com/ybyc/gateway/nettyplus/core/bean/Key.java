package com.ybyc.gateway.nettyplus.core.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * klv 具体项，
 * 只有Groups才会触发 Key解析
 */
@Exclude
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Key {

    long value();

}
