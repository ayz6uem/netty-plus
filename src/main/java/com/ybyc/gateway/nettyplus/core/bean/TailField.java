package com.ybyc.gateway.nettyplus.core.bean;

import java.lang.annotation.*;

/**
 * 字段位置配置，末尾字段
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TailField {

}
