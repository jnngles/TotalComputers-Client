package com.jnngl.client;

import com.jnngl.client.protocol.*;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

public class PacketHandler extends ChannelDuplexHandler {

    private static int freeId = 0;

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

    private void handlePingS2C(ClientboundPingPacket s2c_ping) {
        ServerboundPongPacket c2s_pong = new ServerboundPongPacket();
        c2s_pong.payload = s2c_ping.payload;
        ctx.writeAndFlush(c2s_pong);
    }

    private void handleCreationRequestS2C(ClientboundCreationRequestPacket s2c_request) {
        // TODO: Implement this
        ServerboundCreationStatusPacket c2s_status = new ServerboundCreationStatusPacket();
        c2s_status.status = ServerboundCreationStatusPacket.STATUS_OK;
        if(freeId > Short.MAX_VALUE) c2s_status.status = ServerboundCreationStatusPacket.STATUS_ERR;
        else c2s_status.id = (short)(freeId++);
        ctx.writeAndFlush(c2s_status);
    }

    private void handleDestroyS2C(ClientboundDestroyPacket s2c_destroy) {
        // TODO: Implement this
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        this.ctx = ctx;
        if(msg instanceof ClientboundHandshakePacket packet) handleHandshakeS2C(packet);
        else if(msg instanceof ClientboundDisconnectPacket packet) handleDisconnectS2C(packet);
        else if(msg instanceof ClientboundConnectionSuccessPacket packet) handleConnectionSuccessS2C(packet);
        else if(msg instanceof ClientboundPingPacket packet) handlePingS2C(packet);
        else if(msg instanceof ClientboundCreationRequestPacket packet) handleCreationRequestS2C(packet);
        else if(msg instanceof ClientboundDestroyPacket packet) handleDestroyS2C(packet);
        super.channelRead(ctx, msg);
    }



}
