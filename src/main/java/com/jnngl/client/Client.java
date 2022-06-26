package com.jnngl.client;

import com.jnngl.client.exception.IncompatibleAPIException;
import com.jnngl.client.exception.PacketAlreadyExistsException;
import com.jnngl.client.protocol.Packet;
import com.jnngl.client.protocol.Protocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;

public class Client {

    public static final String VERSION = "0.10b-p1";

    public static boolean DEBUG = false;
    public static boolean LOGGER = true;

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

    private static void checkUpdates() {
        try {
            Scanner scanner = new Scanner(new URL("https://raw.githubusercontent.com/JNNGL/TotalComputers-Client/main/VERSION").openStream());
            String latestVersion = scanner.nextLine();
            if(!latestVersion.equals(VERSION)) {
                System.out.println(Localization.get(22)+latestVersion);
                System.out.println(Localization.get(24)+"https://github.com/JNNGL/TotalComputers-Client/releases\n");
            }
        } catch (IOException e) {
            System.err.println(Localization.get(23));
        }
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
                ch.config().setOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());
                ch.config().setOption(ChannelOption.TCP_NODELAY, true);
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
        return 2;
    }

    public static void main(String[] args) throws PacketAlreadyExistsException, NoSuchMethodException, IOException,
            ClassNotFoundException, InvocationTargetException, IllegalAccessException, IncompatibleAPIException {
        for(String arg : args) {
            if (arg.equalsIgnoreCase("--debug"))
                Client.DEBUG = true;
            else if(arg.equalsIgnoreCase("--no-logger"))
                Client.LOGGER = false;
            else if(arg.equalsIgnoreCase("--ru-lang"))
                Localization.init(new Localization.HeccrbqZpsr());
            else if(arg.equalsIgnoreCase("--en-lang"))
                Localization.init(new Localization.EnglishLang());
        }
        if(Client.LOGGER)
            Logger.initializeLogger();

        if(Localization.get() == null) {
            if(Locale.getDefault().getLanguage().equals(new Locale("ru").getLanguage()))
                 Localization.init(new Localization.HeccrbqZpsr());
            else Localization.init(new Localization.EnglishLang());
        }

        checkUpdates();

        if(Client.DEBUG)
            System.out.println(Localization.get(0));

        final Scanner scanner = new Scanner(System.in);
        System.out.print(Localization.get(1));
        final String ip = scanner.next();
        System.out.print(Localization.get(2));
        final int port = scanner.nextInt();
        System.out.print(Localization.get(3));
        final String token = scanner.next();

        System.out.println("\n-------------------");

        Client client = new Client();
        client.setToken(token);

        System.out.println(Localization.get(4));
        client.loadCore(new File("core.jar"));

        client.api = (int) client.core.findClass("com.jnngl.totalcomputers.system.TotalOS")
                .getMethod("getApiVersion").invoke(null);
        if(client.getCoreApiVersion() < 8)
            throw new IncompatibleAPIException(client.getCoreApiVersion(), 8);
        System.out.println(Localization.get(5)+client.getCoreApiVersion());

        System.out.println(Localization.get(6));
        Protocol.registerPackets();
        System.out.println(Localization.get(7)+Packet.totalRegisteredPackets()+Localization.get(8));

        System.out.println("-------------------\n");
        client.start(ip, port);
    }

}
