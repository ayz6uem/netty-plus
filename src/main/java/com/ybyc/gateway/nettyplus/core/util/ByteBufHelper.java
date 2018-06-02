package com.ybyc.gateway.nettyplus.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.ByteOrder;

/**
 * 字节操作辅助类
 * @author wangzhe
 */
public class ByteBufHelper {

    public static void set(ByteBuf buf, int offset, int length, ByteOrder byteOrder, long value) {
        if(ByteOrder.BIG_ENDIAN.equals(byteOrder)){
            set(buf,offset,length,value);
        }else{
            setLE(buf,offset,length,value);
        }
    }

    public static void set(ByteBuf buf,int offset, int length,long value) {
        switch (length) {
            case 1:
                buf.setByte(offset,(int)value);
                break;
            case 2:
                buf.setShort(offset,(int)value);
                break;
            case 3:
                buf.setMedium(offset,(int)value);
                break;
            case 4:
                buf.setInt(offset,(int)value);
                break;
            case 8:
                buf.setLong(offset,value);
                break;
            default:
                throw new IllegalArgumentException("unset bytes");
        }
    }

    public static void setLE(ByteBuf buf,int offset, int length,long value) {
        switch (length) {
            case 1:
                buf.setByte(offset,(int)value);
                break;
            case 2:
                buf.setShortLE(offset,(int)value);
                break;
            case 3:
                buf.setMediumLE(offset,(int)value);
                break;
            case 4:
                buf.setIntLE(offset,(int)value);
                break;
            case 8:
                buf.setLongLE(offset,value);
                break;
            default:
                throw new IllegalArgumentException("unset bytes");
        }
    }

    public static Number get(ByteBuf buf,int offset, int length, ByteOrder byteOrder){
        if(ByteOrder.BIG_ENDIAN.equals(byteOrder)){
            return get(buf,offset,length);
        }else{
            return getLE(buf,offset,length);
        }
    }

    public static Number get(ByteBuf buf,int offset, int length) {
        Number value;
        switch (length) {
            case 1:
                value = buf.getByte(offset);
                break;
            case 2:
                value = buf.getShort(offset);
                break;
            case 3:
                value = buf.getMedium(offset);
                break;
            case 4:
                value = buf.getInt(offset);
                break;
            case 8:
                value = buf.getLong(offset);
                break;
            default:
                throw new IllegalArgumentException("unread bytes");
        }
        return value;
    }

    public static Number getLE(ByteBuf buf,int offset, int length) {
        Number value;
        switch (length) {
            case 1:
                value = buf.getByte(offset);
                break;
            case 2:
                value = buf.getShortLE(offset);
                break;
            case 3:
                value = buf.getMediumLE(offset);
                break;
            case 4:
                value = buf.getIntLE(offset);
                break;
            case 8:
                value = buf.getLongLE(offset);
                break;
            default:
                throw new IllegalArgumentException("unread bytes");
        }
        return value;
    }

    public static Object readPrimitive(ByteBuf byteBuf,int bytes,ByteOrder byteOrder,boolean unsigned) {
        if(unsigned){
            return readUnSigned(byteBuf,bytes,byteOrder);
        }else{
            return read(byteBuf,bytes,byteOrder);
        }
    }

    public static Object read(ByteBuf buf, int length, ByteOrder byteOrder){
        if(ByteOrder.BIG_ENDIAN.equals(byteOrder)){
            return read(buf,length);
        }else{
            return readLE(buf,length);
        }
    }

    public static Object readUnSigned(ByteBuf buf, int length, ByteOrder byteOrder){
        if(ByteOrder.BIG_ENDIAN.equals(byteOrder)){
            return readUnSigned(buf,length);
        }else{
            return readUnSignedLE(buf,length);
        }
    }

    public static Object read(ByteBuf buf, int length) {
        Object value;
        switch (length) {
            case 1:
                value = buf.readByte();
                break;
            case 2:
                value = buf.readShort();
                break;
            case 3:
                value = buf.readMedium();
                break;
            case 4:
                value = buf.readInt();
                break;
            case 8:
                value = buf.readLong();
                break;
            default:
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                value = bytes;
        }
        return value;
    }

    public static Object readLE(ByteBuf buf, int length) {
        Object value;
        switch (length) {
            case 1:
                value = buf.readByte();
                break;
            case 2:
                value = buf.readShortLE();
                break;
            case 3:
                value = buf.readMediumLE();
                break;
            case 4:
                value = buf.readIntLE();
                break;
            case 8:
                value = buf.readLongLE();
                break;
            default:
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                value = bytes;
        }
        return value;
    }

    public static Object readUnSigned(ByteBuf buf, int length) {
        Object value;
        switch (length) {
            case 1:
                value = buf.readUnsignedByte();
                break;
            case 2:
                value = buf.readUnsignedShort();
                break;
            case 3:
                value = buf.readUnsignedMedium();
                break;
            case 4:
                value = buf.readUnsignedInt();
                break;
            case 8:
                value = buf.readLong();
                break;
            default:
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                value = bytes;
        }
        return value;
    }


    public static Object readUnSignedLE(ByteBuf buf, int length) {
        Object value;
        switch (length) {
            case 1:
                value = buf.readUnsignedByte();
                break;
            case 2:
                value = buf.readUnsignedShortLE();
                break;
            case 3:
                value = buf.readUnsignedMediumLE();
                break;
            case 4:
                value = buf.readUnsignedIntLE();
                break;
            case 8:
                value = buf.readLongLE();
                break;
            default:
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                value = bytes;
        }
        return value;
    }

    public static void write(ByteBuf buf, int length, Number value, ByteOrder byteOrder,boolean unsiged){
        if(!unsiged){
            write(buf,length,value,byteOrder);
        }else{
            writeUnsigned(buf,length,value,byteOrder);
        }
    }
    public static void write(ByteBuf buf, int length, Number value, ByteOrder byteOrder){
        if(ByteOrder.BIG_ENDIAN.equals(byteOrder)){
            write(buf,length,value);
        }else{
            writeLE(buf,length,value);
        }
    }

    public static void write(ByteBuf buf, int length, Number value) {
        switch (length) {
            case 1:
                buf.writeByte(value.byteValue());
                break;
            case 2:
                buf.writeShort(value.shortValue());
                break;
            case 3:
                buf.writeMedium(value.intValue());
                break;
            case 4:
                buf.writeInt(value.intValue());
                break;
            case 8:
                buf.writeLong(value.longValue());
                break;
            default:
        }
    }

    public static void writeLE(ByteBuf buf, int length, Number value) {
        switch (length) {
            case 1:
                buf.writeByte(value.byteValue());
                break;
            case 2:
                buf.writeShortLE(value.shortValue());
                break;
            case 3:
                buf.writeMediumLE(value.intValue());
                break;
            case 4:
                buf.writeIntLE(value.intValue());
                break;
            case 8:
                buf.writeLongLE(value.longValue());
                break;
            default:
        }
    }

    public static void writeUnsigned(ByteBuf buf, int length, Number value, ByteOrder byteOrder){
        if(ByteOrder.BIG_ENDIAN.equals(byteOrder)){
            writeUnsigned(buf,length,value);
        }else{
            writeUnsignedLE(buf,length,value);
        }
    }

    public static void writeUnsigned(ByteBuf buf, int length, Number value) {
        switch (length) {
            case 1:
            case 2:
            case 3:
                buf.writeByte(value.byteValue());
                break;
            case 4:
                buf.writeShort(value.shortValue());
                break;
            case 8:
                buf.writeInt(value.intValue());
                break;
            default:
        }
    }

    public static void writeUnsignedLE(ByteBuf buf, int length, Number value) {
        switch (length) {
            case 1:
            case 2:
            case 3:
                buf.writeByte(value.byteValue());
                break;
            case 4:
                buf.writeShortLE(value.shortValue());
                break;
            case 8:
                buf.writeIntLE(value.intValue());
                break;
            default:
        }
    }


    public static String binaryDump(byte[] bytes) {
        StringBuffer result=new StringBuffer();
        for(int i=0;i<bytes.length;i++){
            for(int j=0;j<Byte.SIZE;j++){
                result.append(Integer.toBinaryString(((bytes[i] << j) & 0xff) >>> Byte.SIZE-1));
            }
        }
        return result.toString();
    }


    public static String binaryDump(ByteBuf buf) {
        StringBuffer result=new StringBuffer();
        for(int i=0;i<buf.readableBytes();i++){
            for(int j=0;j<Byte.SIZE;j++){
                result.append(Integer.toBinaryString(((buf.getByte(i) << j) & 0xff) >>> Byte.SIZE-1));
            }
        }
        return result.toString();
    }

    public static byte[] decodeBinaryDump(String value) {
        char[] chars=value.toCharArray();
        int length = chars.length%Byte.SIZE==0?chars.length/Byte.SIZE:chars.length/Byte.SIZE+1;
        byte[] result = new byte[length];
        for(int i=0;i<chars.length;i++){
            int index = i/Byte.SIZE;
            int left = Byte.SIZE - i%Byte.SIZE-1;
            int b = chars[i]==48?0x00:0x01;
            result[index] |= b << left;
        }
        return result;
    }

    public static byte[] fillBytes(byte[] bytes, int length) {
        if(bytes==null || bytes.length == length){
            return bytes;
        }
        byte[] result = new byte[length];
        for(int i=0;i<bytes.length&&i<length;i++){
            result[i] = bytes[i];
        }
        return result;
    }

    public static ByteBuf insert(ByteBuf buf, int offset, byte b){
        int length = buf.readableBytes();
        ByteBuf result = Unpooled.buffer(length+1);
        if(offset>0){
            result.writeBytes(buf.slice(0,offset));
        }
        result.writeByte(b);
        if(offset<length){
            result.writeBytes(buf.slice(offset,length-offset));
        }
        return result;
    }
}
