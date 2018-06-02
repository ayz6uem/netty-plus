package com.ybyc.gateway.nettyplus.core.key;

import com.ybyc.gateway.nettyplus.core.bean.Option;
import io.netty.buffer.ByteBuf;

public class KeyData {

    private byte key;
    private byte length;
    @Option(lengthField = "length")
    private ByteBuf value;

    public byte getKey() {
        return key;
    }

    public void setKey(byte key) {
        this.key = key;
    }

    public byte getLength() {
        return length;
    }

    public void setLength(byte length) {
        this.length = length;
    }

    public ByteBuf getValue() {
        return value;
    }

    public void setValue(ByteBuf value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Key{" +
                "key=" + key +
                ", length=" + length +
                ", value=" + value +
                '}';
    }
}
