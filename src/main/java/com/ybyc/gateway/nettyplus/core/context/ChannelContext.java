package com.ybyc.gateway.nettyplus.core.context;

import com.ybyc.gateway.nettyplus.core.exception.ChannelNotFoundException;
import com.ybyc.gateway.nettyplus.core.util.Assert;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * 客户端连接下文
 * 持有，保存连接，
 * @author wangzhe
 */
public class ChannelContext {

    public static final String BIZ_ID = "_bizId";
    public static final AttributeKey<Object> BIZ_ID_KEY = AttributeKey.newInstance(BIZ_ID);

    private static ChannelContext instance;

    private Map<Object,Channel> channelMap = new ConcurrentHashMap<>();

    private BiConsumer<Object,Channel> onlineConsumer;
    private BiConsumer<Object,Channel> offlineConsumer;

    public static ChannelContext getInstance() {
        if(instance==null){
            newInstance();
        }
        return instance;
    }

    private static synchronized ChannelContext newInstance(){
        if(instance==null){
            instance = new ChannelContext();
        }
        return instance;
    }

    public void setOnlineConsumer(BiConsumer<Object, Channel> onlineConsumer) {
        this.onlineConsumer = onlineConsumer;
    }

    public void setOfflineConsumer(BiConsumer<Object, Channel> offlineConsumer) {
        this.offlineConsumer = offlineConsumer;
    }

    public boolean contain(Object id){
        return channelMap.containsKey(id);
    }

    public Channel find(Object id){
        Channel channel = channelMap.get(id);
        if(Objects.isNull(channel)){
            throw new ChannelNotFoundException("channel not found of "+id);
        }
        return channel;
    }

    public Channel online(Object id,Channel channel){
        Assert.notNull(id,"id can not be null");
        channel.attr(BIZ_ID_KEY).set(id);
        if(Objects.nonNull(onlineConsumer)){
            onlineConsumer.accept(id,channel);
        }
        return channelMap.put(id,channel);
    }

    public void offline(Channel channel){
        Object id = channel.attr(ChannelContext.BIZ_ID_KEY).get();
        if(Objects.nonNull(id)){
            if(Objects.nonNull(offlineConsumer)){
                offlineConsumer.accept(id,channel);
            }
            channelMap.remove(id);
        }
    }

    public Set<Object> keys(){
        return channelMap.keySet();
    }

    public Iterator<Map.Entry<Object,Channel>> iterator(){
        return channelMap.entrySet().iterator();
    }

    public int size(){
        return channelMap.size();
    }

    public static Object getId(Channel channel) {
        return channel.attr(ChannelContext.BIZ_ID_KEY).get();
    }

    public static boolean isOnline(Channel channel) {
        return Objects.nonNull(channel.attr(ChannelContext.BIZ_ID_KEY).get());
    }
}
