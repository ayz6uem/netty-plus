package com.ybyc.gateway.nettyplus.core.codec;

import com.ybyc.gateway.nettyplus.core.TcpServer;
import com.ybyc.gateway.nettyplus.core.util.ByteBufHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.ByteOrder;
import java.util.List;

/**
 * 根据整个帧长度，纠正length位的值
 * @author wangzhe
 */
public class LengthFieldBasedFrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    private int offset;

    /**
     * length默认值为length后字节数
     */
    private int length;

    /**
     * 对length进行纠正
     */
    private int adjustment;

    ByteOrder byteOrder;

    public LengthFieldBasedFrameEncoder(int offset, int length) {
        this(offset,length,0);
    }

    public LengthFieldBasedFrameEncoder(int offset, int length, int adjustment) {
        this(TcpServer.Options.DEFAULT_BYTEORDER,offset,length,adjustment);
    }
    public LengthFieldBasedFrameEncoder(ByteOrder byteOrder, int offset, int length, int adjustment) {
        this.offset = offset;
        this.length = length;
        this.adjustment = adjustment;
        this.byteOrder = byteOrder;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.retain();
        int value = msg.readableBytes() - offset - length - adjustment;
        ByteBufHelper.set(msg,offset,length, byteOrder,value);
        out.add(msg);
    }
}
