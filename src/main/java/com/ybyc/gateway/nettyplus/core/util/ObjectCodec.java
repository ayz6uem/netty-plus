package com.ybyc.gateway.nettyplus.core.util;

import io.netty.buffer.ByteBuf;

/**
 * 通用编解码器，提供ByteBuf与Object对应转换能力
 * public class Foo<T>{
 *     Header header;
 *     T body;
 * }
 * Foo<Content> foo = ObjectCodec.just(byteBuf).decode(new Foo<Content>(){});
 * attention "{}"，or i will not get Content.class
 * @author wangzhe
 */
public class ObjectCodec {

    public static ObjectDecoder just(ByteBuf byteBuf){
        return new ObjectDecoder(byteBuf);
    }

    public static ObjectEncoder just(Object object){
        return new ObjectEncoder(object);
    }

}
