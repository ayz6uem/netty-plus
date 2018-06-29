package com.ybyc.gateway.nettyplus.core.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 错误处理，拦截
 * @author wangzhe
 */
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    Consumer<Throwable> exceptionConsumer;

    public ExceptionHandler() {
    }

    public ExceptionHandler(Consumer<Throwable> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(Objects.nonNull(exceptionConsumer)){
            exceptionConsumer.accept(cause);
        }else{
            logger.error(cause.getMessage(),cause);
        }
    }
}
