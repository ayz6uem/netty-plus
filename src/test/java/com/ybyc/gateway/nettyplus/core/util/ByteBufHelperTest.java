package com.ybyc.gateway.nettyplus.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Test;

public class ByteBufHelperTest {

    @Test
    public void test1(){
        ByteBuf byteBuf = Unpooled.copiedBuffer(new byte[]{0x01,0x02,0x03,0x04,0x05});
        byteBuf = ByteBufHelper.insert(byteBuf,5,(byte)0x0A);
        System.out.println(ByteBufUtil.hexDump(byteBuf));
    }

}
