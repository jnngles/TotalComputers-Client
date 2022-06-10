package com.jnngl.client.protocol;

import com.jnngl.client.BufUtils;
import com.jnngl.client.exception.TooSmallPacketException;
import io.netty.buffer.ByteBuf;

public class ClientboundHandshakePacket extends Packet {

    @Override
    public byte getPacketID() {
        return (byte)0xB1;
    }

    @Override
    public void writeData(ByteBuf buf) {
        BufUtils.writeString(buf, serverName);
    }

    @Override
    public void readData(ByteBuf buf, int length) throws Exception {
        if(length < 4) throw new TooSmallPacketException(length, 4);
        serverName = BufUtils.readString(buf);
    }

    @Override
    public int getLength() {
        return BufUtils.sizeofString(serverName);
    }

    public String serverName;

}
