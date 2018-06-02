package com.ybyc.gateway.nettyplus.core.key;

import com.ybyc.gateway.nettyplus.core.bean.Groups;
import com.ybyc.gateway.nettyplus.core.bean.Key;
import com.ybyc.gateway.nettyplus.core.bean.Option;
import com.ybyc.gateway.nettyplus.core.bean.StringOption;

public class Foo {

    private Byte result;
    private Integer id;
    @Groups
    private Byte groups;

    @Key(2)
    private Short port;
    @Key(1)
    @Option(value = 4,string = StringOption.HEX)
    private String ip;

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getGroups() {
        return groups;
    }

    public void setGroups(byte groups) {
        this.groups = groups;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Foo{" +
                "result=" + result +
                ", id=" + id +
                ", groups=" + groups +
                ", ip=" + ip +
                ", port=" + port +
                '}';
    }
}
