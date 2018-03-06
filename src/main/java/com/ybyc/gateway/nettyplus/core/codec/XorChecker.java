package com.ybyc.gateway.nettyplus.core.codec;

import com.ybyc.gateway.nettyplus.core.exception.BytesCheckException;
import com.ybyc.gateway.nettyplus.core.util.SumHelper;
import com.ybyc.gateway.nettyplus.core.util.XorHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

/**
 * 对帧末尾的校验位进行xor校验
 * xor 默认数据类型为byte，一个字节
 * @author wangzhe
 */
public class XorChecker extends MessageToMessageCodec<ByteBuf,ByteBuf> {

    private int checkByteIndex = -1;

    public XorChecker() {
    }

    public XorChecker(int checkByteIndex) {
        this.checkByteIndex = checkByteIndex;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.retain();
        byte xor = XorHelper.loop(msg,msg.readableBytes()+checkByteIndex);
        msg.setByte(msg.readableBytes()+checkByteIndex,xor);
        out.add(msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.retain();
        byte loopxor = XorHelper.loop(msg,msg.readableBytes()+checkByteIndex);
        byte xor = msg.getByte(msg.readableBytes()+checkByteIndex);
        if(loopxor != xor){
            throw new BytesCheckException("error check "+ByteBufUtil.hexDump(msg).toUpperCase());
        }
        out.add(msg);
    }

}
