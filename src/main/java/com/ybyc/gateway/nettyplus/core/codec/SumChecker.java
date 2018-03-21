package com.ybyc.gateway.nettyplus.core.codec;

import com.ybyc.gateway.nettyplus.core.exception.BytesCheckException;
import com.ybyc.gateway.nettyplus.core.util.SumHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 对帧末尾的校验位进行Sum校验
 * sum 默认数据类型为byte，一个字节
 *
 * @author wangzhe
 */
@ChannelHandler.Sharable
public class SumChecker extends MessageToMessageCodec<ByteBuf, ByteBuf> {

    private int bytesOffset = 0;
    private int checkByteIndex = -1;

    public SumChecker() {
    }

    public SumChecker(int checkByteIndex) {
        this.checkByteIndex = checkByteIndex;
    }

    public SumChecker(int bytesOffset, int checkByteIndex) {
        this.bytesOffset = bytesOffset;
        this.checkByteIndex = checkByteIndex;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.retain();
        byte sum = SumHelper.loop(msg, bytesOffset, msg.readableBytes() + checkByteIndex);
        msg.setByte(msg.readableBytes() + checkByteIndex, sum);
        out.add(msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.retain();
        byte loopSum = SumHelper.loop(msg, bytesOffset, msg.readableBytes() + checkByteIndex);
        byte sum = msg.getByte(msg.readableBytes() + checkByteIndex);
        if (loopSum != sum) {
            throw new BytesCheckException("loop:" + loopSum + " sum:" + sum + " bytes:" + ByteBufUtil.hexDump(msg).toUpperCase());
        }
        out.add(msg);
    }

}
