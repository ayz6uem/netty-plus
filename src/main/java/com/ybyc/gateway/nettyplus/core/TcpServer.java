package com.ybyc.gateway.nettyplus.core;

import com.ybyc.gateway.nettyplus.core.context.ChannelContext;
import com.ybyc.gateway.nettyplus.core.context.TaskContext;
import com.ybyc.gateway.nettyplus.core.handler.ConnectionChannelHandler;
import com.ybyc.gateway.nettyplus.core.handler.ExceptionHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.nio.ByteOrder;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Server 构建启动类
 * @author wangzhe
 */
public class TcpServer {

    public static TcpServer create(int port) {
        return options().port(port).build();
    }
    public static TcpServer create(Consumer<Options> optionsConsumer) {
        Options options = options();
        if(Objects.nonNull(optionsConsumer)){
            optionsConsumer.accept(options);
        }
        return options.build();
    }

    private Consumer<ChannelPipeline> pipelineConsumer;
    private Consumer<Channel> startedConsumer;
    private Consumer<Object> stoppedSupplier;
    private Consumer<Throwable> exceptionConsumer;
    private BiConsumer<ChannelHandlerContext,IdleStateEvent> eventBiConsumer;

    private TcpServer.Options options;

    public TcpServer(TcpServer.Options options){
        this.options = options;
    }

    public void start() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ExceptionHandler(exceptionConsumer));
                            ch.pipeline().addLast(new IdleStateHandler(options.readIdle, options.writeIdle, options.allIdle, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new ConnectionChannelHandler());
                            if(pipelineConsumer!=null){
                                pipelineConsumer.accept(ch.pipeline());
                            }
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, options.backlog)
                    .childOption(ChannelOption.SO_KEEPALIVE, options.keepalive);

            TaskContext.getInstance().start();
            ChannelFuture f = bootstrap.bind(options.port).sync();

            if(startedConsumer!=null){
                startedConsumer.accept(f.channel());
            }

            f.channel().closeFuture().sync();
        } finally {
            TaskContext.getInstance().stop();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            if(stoppedSupplier!=null){
                stoppedSupplier.accept(null);
            }
        }
    }

    public TcpServer onPipeline(Consumer<ChannelPipeline> pipelineConsumer){
        this.pipelineConsumer = pipelineConsumer;
        return this;
    }

    public TcpServer onStart(Consumer<Channel> startedConsumer){
        this.startedConsumer = startedConsumer;
        return this;
    }

    public TcpServer onShutdown(Consumer<Object> stoppedSupplier){
        this.stoppedSupplier = stoppedSupplier;
        return this;
    }

    public TcpServer onOnline(BiConsumer<Object, Channel> onlineConsumer){
        ChannelContext.getInstance().setOnlineConsumer(onlineConsumer);
        return this;
    }

    public TcpServer onOffline(BiConsumer<Object, Channel> offlineConsumer){
        ChannelContext.getInstance().setOfflineConsumer(offlineConsumer);
        return this;
    }

    public TcpServer onException(Consumer<Throwable> exceptionConsumer){
        this.exceptionConsumer = exceptionConsumer;
        return this;
    }

    public TcpServer onEvent(BiConsumer<ChannelHandlerContext,IdleStateEvent> eventBiConsumer){
        this.eventBiConsumer = eventBiConsumer;
        return this;
    }

    public static TcpServer.Options options(){
        return new TcpServer.Options();
    }
    public static class Options{
        public int port = 80;
        public int backlog = 128;
        public boolean keepalive = true;
        public int writeIdle = 60;
        public int readIdle = 60;
        public int allIdle = 120;
        public static ByteOrder DEFAULT_BYTEORDER = ByteOrder.BIG_ENDIAN;

        public Options port(int port) {
            this.port = port;
            return this;
        }

        public Options backlog(int so_backlog) {
            this.backlog = so_backlog;
            return this;
        }

        public Options keepalive(boolean so_keepalive) {
            this.keepalive = so_keepalive;
            return this;
        }

        public Options idle(int writeIdle, int readIdle, int allIdle){
            this.writeIdle = writeIdle;
            this.readIdle = readIdle;
            this.allIdle = allIdle;
            return this;
        }

        public Options byteOrder(ByteOrder byteOrder){
            DEFAULT_BYTEORDER = byteOrder;
            return this;
        }

        public TcpServer build(){
            TcpServer server = new TcpServer(this);
            return server;
        }
    }

}