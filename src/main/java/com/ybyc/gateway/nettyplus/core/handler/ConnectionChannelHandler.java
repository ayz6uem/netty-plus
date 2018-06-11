package com.ybyc.gateway.nettyplus.core.handler;

import com.ybyc.gateway.nettyplus.core.context.ChannelContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 链接离线处理，心跳事件处理
 * @author wangzhe
 */
public class ConnectionChannelHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    ChannelContext channelContext = ChannelContext.getInstance();
    BiConsumer<ChannelHandlerContext,IdleStateEvent> eventBiConsumer;

    Consumer<Throwable> exceptionConsumer;

    public ConnectionChannelHandler() {
    }

    public ConnectionChannelHandler(BiConsumer<ChannelHandlerContext, IdleStateEvent> eventBiConsumer) {
        this.eventBiConsumer = eventBiConsumer;
    }

    public ConnectionChannelHandler(BiConsumer<ChannelHandlerContext, IdleStateEvent> eventBiConsumer, Consumer<Throwable> exceptionConsumer) {
        this.eventBiConsumer = eventBiConsumer;
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        if(logger.isDebugEnabled()){
            logger.info("channelRegistered:{}",ctx.channel().remoteAddress());
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        if(logger.isDebugEnabled()){
            logger.info("channelUnregistered:{}",ctx.channel().remoteAddress());
        }
        ctx.channel().close();
        channelContext.offline(ctx.channel());
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if(Objects.nonNull(eventBiConsumer)){
                eventBiConsumer.accept(ctx,idleStateEvent);
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(),cause);
        if(Objects.nonNull(exceptionConsumer)){
            exceptionConsumer.accept(cause);
        }
    }
}
