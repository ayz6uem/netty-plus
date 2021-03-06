package com.ybyc.gateway.nettyplus.core;

import com.ybyc.gateway.nettyplus.core.codec.Directive;
import com.ybyc.gateway.nettyplus.core.codec.DirectiveCodec;
import com.ybyc.gateway.nettyplus.core.codec.FixedHeadLengthFieldBasedFrameDecoder;
import com.ybyc.gateway.nettyplus.core.codec.LengthFieldBasedFrameEncoder;
import com.ybyc.gateway.nettyplus.core.context.ChannelContext;
import com.ybyc.gateway.nettyplus.core.context.TaskContext;
import com.ybyc.gateway.nettyplus.core.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;
import java.rmi.NoSuchObjectException;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Server 构建启动类
 *
 * @author wangzhe
 */
public class TcpServer {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 使用默认配置构建TcpServer
     * @param port
     * @return
     */
    public static TcpServer create(int port) {
        return options().port(port).build();
    }

    /**
     * 自定义配置构建TcpServer
     * @param optionsConsumer
     * @return
     */
    public static TcpServer create(Consumer<Options> optionsConsumer) {
        Options options = options();
        if (Objects.nonNull(optionsConsumer)) {
            optionsConsumer.accept(options);
        }
        return options.build();
    }

    private Consumer<ChannelPipeline> pipelineConsumer;
    private Consumer<Channel> startedConsumer;
    private Consumer<Object> stoppedSupplier;
    private Consumer<Throwable> exceptionConsumer;
    private BiConsumer<ChannelHandlerContext, IdleStateEvent> eventBiConsumer;
    private BiConsumer<Channel,Directive> interceptBiConsumer;

    /**
     * 服务启动配置实例
     */
    private TcpServer.Options options;

    public TcpServer(TcpServer.Options options) {
        this.options = options;
    }

    public void start() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            if(options.triggerIdle){
                                //读写超时处理，用户判断心跳超时
                                ch.pipeline().addLast(IdleStateHandler.class.getSimpleName(), new IdleStateHandler(options.readIdle, options.writeIdle, options.allIdle, TimeUnit.SECONDS));
                            }
                            //异常统一处理 链接处理，链接断开，读写超时事件捕获
                            ch.pipeline().addLast(ConnectionChannelHandler.class.getSimpleName(), new ConnectionChannelHandler(eventBiConsumer));

                            if(Objects.nonNull(options.sliceFrameDecoderConsumer)){
                                options.sliceFrameDecoderConsumer.accept(ch.pipeline());
                            }

                            //校验处理器
                            if (Objects.nonNull(options.frameChecker)) {
                                ch.pipeline().addAfter(Options.SLICE_FRAME_DECODER_NAME,"FrameChecker", options.frameChecker);
                            }
                            if(options.printBytes || logger.isDebugEnabled()){
                                ch.pipeline().addAfter(Options.SLICE_FRAME_DECODER_NAME, BytesHandler.class.getSimpleName(), new BytesHandler());
                            }

                            //指令解析器
                            ch.pipeline().addLast(DirectiveCodec.class.getSimpleName(), new DirectiveCodec(options.directiveOffset, options.directiveLength, options.directiveFunction));

                            if(options.intercept){
                                ch.pipeline().addLast(new DirectiveInterceptor(interceptBiConsumer,options.excludeDirective));
                            }

                            if(Objects.nonNull(options.frameLogRecord)){
                                ch.pipeline().addLast(options.frameLogRecord);
                            }
                            //指令处理集合
                            if (Objects.nonNull(options.inboundHandlers)) {
                                options.inboundHandlers.forEach(inboundHandler -> ch.pipeline().addLast(inboundHandler));
                            }
                            //其他管道处理
                            if (Objects.nonNull(pipelineConsumer)) {
                                pipelineConsumer.accept(ch.pipeline());
                            }
                            ch.pipeline().addLast(new ExceptionHandler(exceptionConsumer));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, options.backlog)
                    .childOption(ChannelOption.SO_KEEPALIVE, options.keepalive);

            //启动任务超时计时器
            TaskContext.getInstance().start();
            ChannelFuture f = bootstrap.bind(options.port).sync();

            if (startedConsumer != null) {
                startedConsumer.accept(f.channel());
            }

            f.channel().closeFuture().sync();
        } finally {
            TaskContext.getInstance().stop();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            if (stoppedSupplier != null) {
                stoppedSupplier.accept(null);
            }
        }
    }

    public TcpServer onPipeline(Consumer<ChannelPipeline> pipelineConsumer) {
        this.pipelineConsumer = pipelineConsumer;
        return this;
    }

    public TcpServer onStart(Consumer<Channel> startedConsumer) {
        this.startedConsumer = startedConsumer;
        return this;
    }

    public TcpServer onShutdown(Consumer<Object> stoppedSupplier) {
        this.stoppedSupplier = stoppedSupplier;
        return this;
    }

    public TcpServer onOnline(BiConsumer<Object, Channel> onlineConsumer) {
        ChannelContext.getInstance().setOnlineConsumer(onlineConsumer);
        return this;
    }

    public TcpServer onOffline(BiConsumer<Object, Channel> offlineConsumer) {
        ChannelContext.getInstance().setOfflineConsumer(offlineConsumer);
        return this;
    }

    public TcpServer onException(Consumer<Throwable> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
        return this;
    }

    public TcpServer onEvent(BiConsumer<ChannelHandlerContext, IdleStateEvent> eventBiConsumer) {
        this.eventBiConsumer = eventBiConsumer;
        return this;
    }

    public TcpServer onIntercept(BiConsumer<Channel, Directive> interceptBiConsumer) {
        this.interceptBiConsumer = interceptBiConsumer;
        return this;
    }


    public static TcpServer.Options options() {
        return new TcpServer.Options();
    }

    public static class Options {

        public static final String SLICE_FRAME_DECODER_NAME = "SliceFrameDecoderName";

        public Consumer<ChannelPipeline> sliceFrameDecoderConsumer;

        public int port = 80;
        public int backlog = 128;
        public boolean keepalive = true;
        public boolean triggerIdle = false;
        public int writeIdle = 60;
        public int readIdle = 60;
        public int allIdle = 120;

        public int frameMaxLength = 1024 * 1024;
        public int lengthAdjustment = 0;
        public int lengthInitialBytes = 0;


        public MessageToMessageCodec frameChecker;
        public MessageToMessageCodec frameLogRecord;

        public int directiveOffset = 0;
        public int directiveLength = 1;
        public Function<Integer, Object> directiveFunction;

        public boolean printBytes = false;

        public boolean intercept = false;

        public Object[] excludeDirective;

        //读写数据的字节序
        public static ByteOrder DEFAULT_BYTEORDER = ByteOrder.BIG_ENDIAN;

        public Collection<? extends ChannelInboundHandler> inboundHandlers;

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

        public Options idle(int writeIdle, int readIdle, int allIdle) {
            this.triggerIdle = true;
            this.writeIdle = writeIdle;
            this.readIdle = readIdle;
            this.allIdle = allIdle;
            return this;
        }

        public Options idle(boolean triggerIdle) {
            this.triggerIdle = triggerIdle;
            return this;
        }

        public Options byteOrder(ByteOrder byteOrder) {
            DEFAULT_BYTEORDER = byteOrder;
            return this;
        }

        public Options taskTimeout(int taskTimeout) {
            TaskContext.getInstance().taskTimeout(taskTimeout);
            return this;
        }

        /**
         * 基于帧长度的编码器
         * @param lengthFieldOffset
         * @param lengthFieldLength
         * @param lengthAdjustment
         * @param lengthInitialBytes
         * @return
         */
        public Options lengthField(int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int lengthInitialBytes) {
            sliceFrameDecoderConsumer = pipeline -> {
                pipeline.addLast(SLICE_FRAME_DECODER_NAME,
                        new LengthFieldBasedFrameDecoder(Options.DEFAULT_BYTEORDER,
                                frameMaxLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, lengthInitialBytes,true));
                pipeline.addLast(LengthFieldBasedFrameEncoder.class.getSimpleName(),
                        new LengthFieldBasedFrameEncoder(lengthFieldOffset, lengthFieldLength, lengthAdjustment));

            };
            return this;
        }

        /**
         * 基于帧头和长度的编码器
         * @param lengthFieldOffset
         * @param lengthFieldLength
         * @param lengthAdjustment
         * @param lengthInitialBytes
         * @return
         */
        public Options fixHeadAndLengthField(int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int lengthInitialBytes, byte ... head) {
            sliceFrameDecoderConsumer = pipeline -> {
                pipeline.addLast(SLICE_FRAME_DECODER_NAME,
                        new FixedHeadLengthFieldBasedFrameDecoder(Options.DEFAULT_BYTEORDER,
                                frameMaxLength, lengthFieldOffset, lengthFieldLength,
                                lengthAdjustment, lengthInitialBytes,true, head));
                pipeline.addLast(LengthFieldBasedFrameEncoder.class.getSimpleName(),
                        new LengthFieldBasedFrameEncoder(lengthFieldOffset, lengthFieldLength, lengthAdjustment));

            };
            return this;
        }

        /**
         * 基于分隔符的编码器
         * @param delimiter
         * @return
         */
        public Options delimiter(ByteBuf delimiter) {
            return delimiter(delimiter, true);
        }
        public Options delimiter(ByteBuf delimiter, boolean stripDelimiter) {
            sliceFrameDecoderConsumer = pipeline -> pipeline.addLast(SLICE_FRAME_DECODER_NAME, new DelimiterBasedFrameDecoder(frameMaxLength, stripDelimiter, delimiter));
            return this;
        }

        public Options lengthField(int lengthFieldOffset, int lengthFieldLength) {
            return lengthField(lengthFieldOffset, lengthFieldLength, this.lengthAdjustment, this.lengthInitialBytes);
        }

        public Options frameChecker(MessageToMessageCodec frameChecker) {
            this.frameChecker = frameChecker;
            return this;
        }

        public Options directiveCodec(int directiveOffset, Function<Integer, Object> directiveFunction) {
            return directiveCodec(directiveOffset,this.directiveLength,directiveFunction);
        }

        public Options directiveCodec(int directiveOffset, int directiveLength, Function<Integer, Object> directiveFunction) {
            this.directiveOffset = directiveOffset;
            this.directiveLength = directiveLength;
            this.directiveFunction = directiveFunction;
            return this;
        }

        public Options inboundHandlers(Collection<? extends ChannelInboundHandler> inboundHandlers) {
            this.inboundHandlers = inboundHandlers;
            return this;
        }

        public Options frameLogRecord(MessageToMessageCodec frameLogRecord) {
            this.frameLogRecord = frameLogRecord;
            return this;
        }

        public Options printBytes(boolean printBytes) {
            this.printBytes = printBytes;
            return this;
        }

        public Options intercept(Object ... excludeDirective){
            this.intercept = true;
            this.excludeDirective = excludeDirective;
            return this;
        }

        public TcpServer build() {
            TcpServer server = new TcpServer(this);
            return server;
        }
    }

}
