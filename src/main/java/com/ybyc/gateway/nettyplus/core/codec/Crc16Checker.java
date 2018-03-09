package com.ybyc.gateway.nettyplus.core.codec;

import com.ybyc.gateway.nettyplus.core.exception.BytesCheckException;
import com.ybyc.gateway.nettyplus.core.util.Crc16Helper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

/**
 * 对帧末尾的校验位进行Crc16校验
 * crc16 默认数据类型为short，两字节
 * 默认index为 帧的长度-2，也就是倒数第二个字节
 *
 * @author wangzhe
 */
public class Crc16Checker extends MessageToMessageCodec<ByteBuf, ByteBuf> {

    private int bytesOffset = 0;
    private int checkByteIndex = -2;

    public Crc16Checker() {
    }

    public Crc16Checker(int checkByteIndex) {
        this.checkByteIndex = checkByteIndex;
    }

    public Crc16Checker(int bytesOffset, int checkByteIndex) {
        this.bytesOffset = bytesOffset;
        this.checkByteIndex = checkByteIndex;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.retain();
        short crc = (short) Crc16Helper.loop(msg, bytesOffset, msg.readableBytes() + checkByteIndex);
        msg.setShort(msg.readableBytes() + checkByteIndex, crc);
        out.add(msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.retain();
        short loopCrc = (short) Crc16Helper.loop(msg, bytesOffset, msg.readableBytes() + checkByteIndex);
        short crc = msg.getShort(msg.readableBytes() + checkByteIndex);
        if (loopCrc != crc) {
            throw new BytesCheckException("error check loop:" + loopCrc + " crc:" + crc + ByteBufUtil.hexDump(msg).toUpperCase());
        }
        out.add(msg);
    }

}
