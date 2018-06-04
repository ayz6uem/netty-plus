package com.ybyc.gateway.nettyplus.core.codec;

/**
 * 配合 intercept(DIRECTIVE使用)
 *
 * class frame implements Directive{
 *     Object getDirectiveValue(){
 *         return direcive;
 *     }
 * }
 */
public interface Directive {
    Object getDirectiveValue();
}
