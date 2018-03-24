package com.ybyc.gateway.nettyplus.core.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by wz on 2018/3/24.
 */
public class BytesHandler extends MessageToMessageCodec<ByteBuf,ByteBuf> {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        log.info("<--{}", ByteBufUtil.hexDump(byteBuf).toUpperCase());
        byteBuf.retain();
        list.add(byteBuf);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        log.info("-->{}", ByteBufUtil.hexDump(byteBuf).toUpperCase());
        byteBuf.retain();
        list.add(byteBuf);
    }
}
