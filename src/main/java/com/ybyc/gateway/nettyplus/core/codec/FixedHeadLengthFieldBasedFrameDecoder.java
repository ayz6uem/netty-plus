package com.ybyc.gateway.nettyplus.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

/**
 * 固定头字节，长度字段的解码器
 */
public class FixedHeadLengthFieldBasedFrameDecoder extends LengthFieldBasedFrameDecoder {

    byte[] head;

    public FixedHeadLengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, byte ... head) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
        this.head = head;
    }

    public FixedHeadLengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, byte ... head) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
        this.head = head;
    }

    public FixedHeadLengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast, byte ... head) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
        this.head = head;
    }

    public FixedHeadLengthFieldBasedFrameDecoder(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast, byte ... head) {
        super(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
        this.head = head;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ensureFixedHead(in);
        return super.decode(ctx, in);
    }

    private void ensureFixedHead(ByteBuf in){
        if(isFixedHead(in)){
            return;
        }
        int discardBytes = 0;
        for (int i = 0; i < in.readableBytes(); i++) {
            if(i>0 && head[0] == in.getByte(in.readerIndex() + i)){
                break;
            }
            discardBytes ++;
        }
        if(discardBytes>0){
            in.readerIndex(in.readerIndex() + discardBytes);
        }
        ensureFixedHead(in);
    }

    private boolean isFixedHead(ByteBuf in){
        for (int i = 0; i < head.length && i < in.readableBytes(); i++) {
            if(head[i] != in.getByte(in.readerIndex()+i)){
                return false;
            }
        }
        return true;
    }
}
