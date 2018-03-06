package com.ybyc.gateway.nettyplus.core.util;

import io.netty.buffer.ByteBuf;

/**
 * crc计算
 * @author wangzhe
 */
public class Crc16Helper {

    public static int loop(ByteBuf buf) {
        return loop(buf, 0, buf.readableBytes());
    }
    public static int loop(ByteBuf buf, int offset, int length) {
        int crc = 0xffff;
        for (int i = offset; i < length; i++) {
            crc ^= buf.getByte(i) & 0xff;
            for (int j = 0; j < Byte.SIZE; j++) {
                if ((crc & 0x01) == 1){
                    crc = (crc >> 1) ^ 0xA001;
                } else {
                    crc = crc >> 1;
                }
            }
        }
        return crc;
    }
}
