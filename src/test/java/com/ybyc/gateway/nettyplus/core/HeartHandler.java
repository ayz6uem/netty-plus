package com.ybyc.gateway.nettyplus.core;

import com.ybyc.gateway.nettyplus.core.handler.GenericObjectChannelInboundHandler;
import io.netty.channel.ChannelHandlerContext;

public class HeartHandler extends GenericObjectChannelInboundHandler<Message<Heart>,Heart> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message<Heart> heartMessage) throws Exception {
        System.out.println(heartMessage);
    }
}
