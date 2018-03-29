package com.ybyc.gateway.nettyplus.core.codec;

import com.ybyc.gateway.nettyplus.core.TcpServer;
import com.ybyc.gateway.nettyplus.core.util.ByteBufHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;
import java.util.List;
import java.util.function.Function;

/**
 * 数据结构类自动解码
 * @author wangzhe
 */
public class DirectiveCodec extends MessageToMessageCodec<ByteBuf,Object> {

    public static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_LENGTH = 1;

    private int directiveOffset;
    private int directiveLength;
    private Function<Integer,Object> directiveFunction;

    private ByteOrder byteOrder;

    public DirectiveCodec(Function<Integer,Object> messageCreator) {
        this(DEFAULT_OFFSET,messageCreator);
    }

    public DirectiveCodec(int directiveIndex, Function<Integer,Object> messageCreator) {
        this(directiveIndex,DEFAULT_LENGTH,messageCreator);
    }

    public DirectiveCodec(int directiveOffset, int directiveLength, Function<Integer,Object> directiveFunction) {
        this(TcpServer.Options.DEFAULT_BYTEORDER,directiveOffset,directiveLength,directiveFunction);
    }
    public DirectiveCodec(ByteOrder byteOrder, int directiveOffset, int directiveLength, Function<Integer,Object> directiveFunction) {
        if(directiveFunction==null){
            throw new NullPointerException("messageCreator can not be null");
        }
        this.byteOrder = byteOrder;
        this.directiveOffset = directiveOffset;
        this.directiveLength = directiveLength;
        this.directiveFunction = directiveFunction;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        if(msg instanceof ByteBuf){
            ByteBuf buf = (ByteBuf)msg;
            buf.retain();
            out.add(buf);
        }else{
            ByteBuf byteBuf = ObjectCodec.just(msg).byteOrder(byteOrder).encode();
            out.add(byteBuf);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int directive = ByteBufHelper.get(msg, directiveOffset, directiveLength, TcpServer.Options.DEFAULT_BYTEORDER).intValue();
        Object message = directiveFunction.apply(directive);
        message = ObjectCodec.just(msg).byteOrder(byteOrder).decode(message);
        out.add(message);
    }
}
