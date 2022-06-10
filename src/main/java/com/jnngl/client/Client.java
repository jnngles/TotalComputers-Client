package com.jnngl.client;

import com.jnngl.client.exception.PacketAlreadyExistsException;
import com.jnngl.client.protocol.Protocol;
import com.jnngl.client.protocol.ServerboundHandshakePacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client {

    private final Client client = this;
    private String token;

    private void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    private boolean initialized = false;

    private void start(String ip, int port) {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.remoteAddress(new InetSocketAddress(ip, port));
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(@NotNull SocketChannel ch) {
                ch.pipeline().addLast("decoder", new PacketDecoder());
                ch.pipeline().addLast("encoder", new PacketEncoder());
                ch.pipeline().addLast("packet_handler", new PacketHandler(client));
                ch.pipeline().addLast("exception_handler", new ExceptionHandler());
            }
        });
        try {
            ChannelFuture channelFuture = bootstrap.connect().sync();
            channelFuture.channel().closeFuture().sync();
            group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static short getProtocolVersion() {
        return 1;
    }

    public static void main(String[] args) throws InterruptedException, PacketAlreadyExistsException, NoSuchMethodException {
        final Scanner scanner = new Scanner(System.in);
        System.out.print("Enter IP address (Without port): ");
        final String ip = scanner.next();
        System.out.print("Enter port: ");
        final int port = scanner.nextInt();
        System.out.print("Enter token: ");
        final String token = scanner.next();

        Protocol.registerPackets();

        Client client = new Client();
        client.setToken(token);
        client.start(ip,port);
    }

}
