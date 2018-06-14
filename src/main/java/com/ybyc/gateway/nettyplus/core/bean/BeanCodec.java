package com.ybyc.gateway.nettyplus.core.bean;

import io.netty.buffer.ByteBuf;

/**
 * 通用编解码器，提供ByteBuf与Object对应转换能力
 * public class Foo<T>{
 *     Header header;
 *     T body;
 * }
 * Foo<Content> foo = BeanCodec.just(byteBuf).decode(new Foo<Content>(){});
 * attention "{}"，or i will not get Content.class
 * @author wangzhe
 */
public class BeanCodec {

    public static BeanDecoder just(ByteBuf byteBuf){
        return new BeanDecoder(byteBuf);
    }

    public static BeanEncoder just(Object object){
        return new BeanEncoder(object);
    }

    public static BeanEncoder just(Object object, ByteBuf buf){
        return new BeanEncoder(object,buf);
    }

}
