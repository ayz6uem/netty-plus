package com.ybyc.gateway.nettyplus.core.codec;

import com.ybyc.gateway.nettyplus.core.TcpServer;
import com.ybyc.gateway.nettyplus.core.util.ByteBufHelper;
import com.ybyc.gateway.nettyplus.core.bean.BeanCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;
import java.util.function.BiFunction;

/**
 * 数据结构类
 * 处理双命令字
 * 自动解码
 * @author wangzhe
 */
public class CoupleDirectiveCodec extends MessageToMessageCodec<ByteBuf,Object> {

    public static final int DEFAULT_LENGTH = 1;

    private int firstDirectiveOffset;
    private int firstDirectiveLength;
    private int secondDirectiveOffset;
    private int secondDirectiveLength;
    private BiFunction<Integer,Integer,Object> messageCreator;

    public CoupleDirectiveCodec(int firstDirectiveOffset, int secondDirectiveOffset, BiFunction<Integer,Integer,Object> messageCreator) {
        this(firstDirectiveOffset,DEFAULT_LENGTH,secondDirectiveOffset,DEFAULT_LENGTH,messageCreator);
    }

    public CoupleDirectiveCodec(int firstDirectiveOffset, int firstDirectiveLength,
                                int secondDirectiveOffset, int secondDirectiveLength,
                                BiFunction<Integer,Integer,Object> messageCreator) {
        if(messageCreator==null){
            throw new NullPointerException("messageCreator can not be null");
        }
        this.firstDirectiveOffset = firstDirectiveOffset;
        this.firstDirectiveLength = firstDirectiveLength;
        this.secondDirectiveOffset = secondDirectiveOffset;
        this.secondDirectiveLength = secondDirectiveLength;
        this.messageCreator = messageCreator;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        ByteBuf byteBuf = BeanCodec.just(msg, ctx.alloc().buffer()).encode();
        out.add(byteBuf);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int firstDirective = ByteBufHelper.get(msg, firstDirectiveOffset, firstDirectiveLength, TcpServer.Options.DEFAULT_BYTEORDER).intValue();
        int secondDirective = ByteBufHelper.get(msg, secondDirectiveOffset, secondDirectiveLength, TcpServer.Options.DEFAULT_BYTEORDER).intValue();
        Object message = messageCreator.apply(firstDirective,secondDirective);
        message = BeanCodec.just(msg).decode(message);
        out.add(message);
    }
}
