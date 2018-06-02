package com.ybyc.gateway.nettyplus.core.key;

import com.ybyc.gateway.nettyplus.core.bean.BeanCodec;
import com.ybyc.gateway.nettyplus.core.bean.Exclude;
import com.ybyc.gateway.nettyplus.core.bean.Key;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.ReflectionUtil;

import java.lang.reflect.Field;

public class KeyTest {


    public static void main(String[] args) throws NoSuchFieldException {
        String str = "01000000010201041122334402025421";
        ByteBuf byteBuf = Unpooled.copiedBuffer(ByteBufUtil.decodeHexDump(str));
        for (int i = 0; i < 100; i++) {
            Foo foo = BeanCodec.just(byteBuf).decode(new Foo());
            byteBuf.release();
            System.out.println(foo);
            byteBuf = BeanCodec.just(foo).encode();
            System.out.println(ByteBufUtil.hexDump(byteBuf));
            System.out.println("------------------------ "+i);
        }


    }

}
