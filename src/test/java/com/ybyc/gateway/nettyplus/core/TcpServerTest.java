package com.ybyc.gateway.nettyplus.core;

import com.ybyc.gateway.nettyplus.core.codec.Directive;
import com.ybyc.gateway.nettyplus.core.codec.DirectiveCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Function;

public class TcpServerTest {


    int port = 80;

    public static void main(String[] args) {
        new TcpServerTest().boot();
    }

    public void boot() {
        try {
            Function<Integer, Directive> messageCreator = directive -> {
                switch (directive.byteValue()) {
                    case 1:
                        return new Message<Heart>(){};
                    default:
                        throw new NullPointerException("directive not support "+Integer.toHexString(directive));
                }
            };

            TcpServer.create(port)
                    .onPipeline(
                            pipeline ->
                                    pipeline
                                            .addLast(new DirectiveCodec(messageCreator))
                                            .addLast(new HeartHandler())
                    )
                    .onStart(ch ->{
                        System.out.println("Tcp Server Started on " + port);
                    })
                    .onEvent((ctx,event)->{
                        switch (event.state()){
                            case ALL_IDLE:
                                ctx.channel().close();
                                break;
                            default:
                        }
                    })
                    .onException(throwable -> {
                        if(throwable instanceof IOException){
                            System.out.println(throwable.getMessage());
                        }else{
                            System.out.println(throwable.getMessage()+throwable);
                        }
                    })
                    .start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test1() throws InterruptedException {
        String host = "127.0.0.1";

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast(new DirectiveCodec(directive->{return null;}){})
                            .addLast(new ClientHandler());
                }
            });

            ChannelFuture f = b.connect(host, port).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static class ClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelActive");
            Message<Heart> message = new Message();
            Heart heart = new Heart();
            heart.setId(11231);
            message.setFlag((byte)1);
            message.setPayload(heart);
            ctx.channel().writeAndFlush(message);
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelRegistered");
            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelUnregistered");
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelInactive");
            super.channelInactive(ctx);
        }
    }

}
