package com.jnngl.client;

import com.jnngl.client.protocol.*;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

public class PacketHandler extends ChannelDuplexHandler {

    private ChannelHandlerContext ctx;
    private final Client client;

    public PacketHandler(Client client) {
        this.client = client;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        ServerboundHandshakePacket c2s_handshake = new ServerboundHandshakePacket();
        c2s_handshake.protocolVersion = Client.getProtocolVersion();
        c2s_handshake.apiVersion = 8;
        ctx.writeAndFlush(c2s_handshake);
        super.channelActive(ctx);
    }

    private void handleHandshakeS2C(ClientboundHandshakePacket handshake_s2c) {
        System.out.println("Connecting to "+handshake_s2c.serverName+" server");
        ServerboundConnectPacket c2s_connect = new ServerboundConnectPacket();
        c2s_connect.token = client.getToken();
        ctx.writeAndFlush(c2s_connect);
    }

    private void handleDisconnectS2C(ClientboundDisconnectPacket disconnect_s2c) {
        System.out.println("Disconnected: "+disconnect_s2c.reason);
        ctx.disconnect();
    }

    private void handleConnectionSuccessS2C(ClientboundConnectionSuccessPacket connectSuccess_s2c) {
        System.out.println("Connected to player "+connectSuccess_s2c.name);
    }

    private void handlePingS2C(ClientboundPingPacket c2s_ping) {
        ServerboundPongPacket s2c_pong = new ServerboundPongPacket();
        s2c_pong.payload = c2s_ping.payload;
        ctx.writeAndFlush(s2c_pong);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        this.ctx = ctx;
        if(msg instanceof ClientboundHandshakePacket packet) handleHandshakeS2C(packet);
        else if(msg instanceof ClientboundDisconnectPacket packet) handleDisconnectS2C(packet);
        else if(msg instanceof ClientboundConnectionSuccessPacket packet) handleConnectionSuccessS2C(packet);
        else if(msg instanceof ClientboundPingPacket packet) handlePingS2C(packet);
        super.channelRead(ctx, msg);
    }

}
