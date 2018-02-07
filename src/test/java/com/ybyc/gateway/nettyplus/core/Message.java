package com.ybyc.gateway.nettyplus.core;


public class Message<T> {

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
}
