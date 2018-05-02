package com.ybyc.gateway.nettyplus.core.handler;

import com.ybyc.gateway.nettyplus.core.codec.Directive;
import com.ybyc.gateway.nettyplus.core.context.ChannelContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class DirectiveInterceptor extends MessageToMessageDecoder<Directive> {

    private Object[] excludeDirectives;

    private BiConsumer<Channel,Directive> interceptBiConsumer;

    public DirectiveInterceptor(Object... excludeDirectives) {
        super();
        this.excludeDirectives = excludeDirectives;
    }

    public DirectiveInterceptor(BiConsumer<Channel,Directive> interceptBiConsumer, Object... excludeDirectives) {
        this(excludeDirectives);
        this.interceptBiConsumer = interceptBiConsumer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Directive directive, List list) throws Exception {
        if(ChannelContext.isOnline(channelHandlerContext.channel()) || contain(directive)){
            list.add(directive);
            return;
        }
        if(Objects.nonNull(interceptBiConsumer)){
            interceptBiConsumer.accept(channelHandlerContext.channel(),directive);
        }
    }

    private boolean contain(Directive directive) {
        for (Object o : excludeDirectives) {
            if(Objects.equals(directive.getDirectiveValue(),o)){
                return true;
            }
        }
        return false;
    }

}
