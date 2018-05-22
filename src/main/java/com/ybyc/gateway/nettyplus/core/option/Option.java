package com.ybyc.gateway.nettyplus.core.option;

import java.lang.annotation.*;

/**
 * 对应编解码器配置类
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Option {

    /**
     * 数据长度，字节数，或数组长度，或集合长度
     * @return
     */
    int value() default 1;

    /**
     * 根据某个field获取长度
     * @return
     */
    String lengthField() default "";

    /**
     * 获取基本数据类型时，是否使用无符号解析
     * @return
     */
    boolean unsigned() default false;

    /**
     * 字符串是否需要进行转换，ConvertOption.HEX or ConvertOption.BINARY
     * @return
     */
    StringOption string() default StringOption.NATURAL;


    /**
     * 泛型的标识
     * @return
     */
    String generic() default "T";

    /**
     * 明确泛型的类型，在明确知道泛型类型的情况下使用，优先于componentGeneric
     * 主要用在集合中
     * @return
     */
    Class<?> genericClass() default Object.class;

    boolean required() default true;

}
