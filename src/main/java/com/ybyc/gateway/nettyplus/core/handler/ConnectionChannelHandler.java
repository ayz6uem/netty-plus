package com.ybyc.gateway.nettyplus.core.handler;

import com.ybyc.gateway.nettyplus.core.context.ChannelContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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

    AttributeKey<LocalDateTime> inTime = AttributeKey.valueOf("inTime");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public ConnectionChannelHandler() {
    }

    public ConnectionChannelHandler(BiConsumer<ChannelHandlerContext, IdleStateEvent> eventBiConsumer) {
        this.eventBiConsumer = eventBiConsumer;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        logger.info("-->channel {} in",ctx.channel().remoteAddress());
        ctx.channel().attr(inTime).set(LocalDateTime.now());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        logger.info("<--channel {} out - in at {}",ctx.channel().remoteAddress(),ctx.channel().attr(inTime).get().format(dateTimeFormatter));
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

}
