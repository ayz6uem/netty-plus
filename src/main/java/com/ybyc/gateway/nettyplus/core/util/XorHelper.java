package com.ybyc.gateway.nettyplus.core.util;

import io.netty.buffer.ByteBuf;

/**
 * xor计算
 * @author wangzhe
 */
public class XorHelper {

    public static byte loop(ByteBuf buf) {
        return loop(buf,0,buf.readableBytes());
    }
    public static byte loop(ByteBuf buf,int offset, int length) {
        byte xor = 0x00;
        for (int i = offset; i < length; i++) {
            xor ^= buf.getByte(i);
        }
        return xor;
    }
}
