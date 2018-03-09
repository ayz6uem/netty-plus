package com.ybyc.gateway.nettyplus.core.util;

import io.netty.buffer.ByteBuf;

/**
 * sum计算
 * @author wangzhe
 */
public class SumHelper {

    public static byte loop(ByteBuf buf) {
        return loop(buf,0,buf.readableBytes());
    }
    public static byte loop(ByteBuf buf, int start, int end) {
        byte sum = 0x00;
        for (int i = start; i < end; i++) {
            sum += buf.getByte(i);
        }
        return sum;
    }
}
