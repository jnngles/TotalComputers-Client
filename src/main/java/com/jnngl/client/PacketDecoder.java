package com.jnngl.client;

import com.jnngl.client.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        out.add(Packet.read(in));

        if(Client.DEBUG) {
            for (Object msg : out) {
                System.out.println("S -> C: " + msg.getClass().getSimpleName() +
                        " (0x" + String.format("%x", ((Packet) msg).getPacketID()) + ")");
            }
        }
    }
}
