package com.ybyc.gateway.nettyplus.core.handler;

import com.ybyc.gateway.nettyplus.core.util.OptionHelper;
import com.ybyc.gateway.nettyplus.core.util.ReflectHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 兼容泛型的ChannelInboundHandler
 * @author wangzhe
 */
public abstract class GenericObjectChannelInboundHandler<T,R> extends SimpleChannelInboundHandler<T> {

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        if(super.acceptInboundMessage(msg)){
            Class<?> targetClass = ReflectHelper.getActualClass(this,"R");
            return OptionHelper.containGeneric(msg,targetClass);
        }
        return false;
    }

    @Override
    protected abstract void channelRead0(ChannelHandlerContext channelHandlerContext, T t) throws Exception ;
}
