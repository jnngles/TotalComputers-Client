package com.jnngl.client;

import com.jnngl.client.exception.IncompatibleAPIException;
import com.jnngl.client.exception.PacketAlreadyExistsException;
import com.jnngl.client.protocol.Packet;
import com.jnngl.client.protocol.Protocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client {

    private final Client client = this;
    private String token;
    private Core core;
    private int api;

    private void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    private void loadCore(File file) throws IOException {
        core = new Core();
        core.loadCore(file);
    }

    public Core getCore() {
        return core;
    }

    public int getCoreApiVersion() {
        return api;
    }

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

    public static void main(String[] args) throws PacketAlreadyExistsException, NoSuchMethodException, IOException,
            ClassNotFoundException, InvocationTargetException, IllegalAccessException, IncompatibleAPIException {
        final Scanner scanner = new Scanner(System.in);
        System.out.print("Enter IP address (Without port): ");
        final String ip = scanner.next();
        System.out.print("Enter port: ");
        final int port = scanner.nextInt();
        System.out.print("Enter token: ");
        final String token = scanner.next();

        System.out.println("\n-------------------");

        Client client = new Client();
        client.setToken(token);

        System.out.println("Loading core...");
        client.loadCore(new File("core.jar"));

        client.api = (int) client.core.findClass("com.jnngl.totalcomputers.system.TotalOS")
                .getMethod("getApiVersion").invoke(null);
        if(client.getCoreApiVersion() < 8)
            throw new IncompatibleAPIException(client.getCoreApiVersion(), 8);
        System.out.println("Core API version: "+client.getCoreApiVersion());

        System.out.println("Registering packets...");
        Protocol.registerPackets();
        System.out.println("Registered "+ Packet.totalRegisteredPackets()+" packets");

        System.out.println("-------------------\n");
        client.start(ip, port);
    }

}
