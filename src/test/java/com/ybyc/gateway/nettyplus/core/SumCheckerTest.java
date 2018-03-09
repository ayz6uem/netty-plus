package com.ybyc.gateway.nettyplus.core;

import com.ybyc.gateway.nettyplus.core.util.SumHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Test;

public class SumCheckerTest {

    @Test
    public void test1(){
        String str = "FAF50980005800000000980FFF";
        ByteBuf msg = Unpooled.copiedBuffer(ByteBufUtil.decodeHexDump(str));
        byte loopSum = SumHelper.loop(msg, 5, msg.readableBytes() + (-1));
        byte sum = msg.getByte(msg.readableBytes() + (-1));

        System.out.println("loopSum:"+loopSum);
        System.out.println("sum:"+sum);
    }

}
