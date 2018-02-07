package com.ybyc.gateway.nettyplus.core.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 错误处理，拦截
 * @author wangzhe
 */
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    Consumer<Throwable> exceptionConsumer;

    public ExceptionHandler(Consumer<Throwable> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(Objects.nonNull(exceptionConsumer)){
            exceptionConsumer.accept(cause);
        }
    }
}
