package com.ybyc.gateway.nettyplus.core.delimiter;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class ClientTest {

    public static void main(String[] args) throws InterruptedException {
        new ClientTest().test1();
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
                            .addLast(new IdleStateHandler(10,10,10))
                            .addLast(new ClientHandler());
                }
            });

            ChannelFuture f = b.connect(host, 8080).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static class ClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);

            String str = "7e 3a 2b 01 23 15 a7 54 7d 01 7d 02 7e".replaceAll(" ","");
            ByteBuf byteBuf = Unpooled.copiedBuffer(ByteBufUtil.decodeHexDump(str));
            ctx.writeAndFlush(byteBuf);

        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            if(evt instanceof IdleStateEvent){
                IdleStateEvent event = (IdleStateEvent) evt;
                if(event.state() == IdleState.WRITER_IDLE){
                    String str = "7e 3a 2b 01 23 15 a7 54 7d 01 7d 02 7e".replaceAll(" ","");
                    ByteBuf byteBuf = Unpooled.copiedBuffer(ByteBufUtil.decodeHexDump(str));
                    ctx.writeAndFlush(byteBuf);
                }
            }
        }
    }
}
