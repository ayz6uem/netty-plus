package com.ybyc.gateway.nettyplus.core;


import com.ybyc.gateway.nettyplus.core.codec.Directive;

public class Message<T> implements Directive {

    private byte flag;
    private T payload;

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @Override
    public Object getDirectiveValue() {
        return null;
    }
}
