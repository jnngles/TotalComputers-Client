package com.jnngl.client;

import com.jcraft.jzlib.Deflater;
import com.jcraft.jzlib.DeflaterOutputStream;
import com.jcraft.jzlib.JZlib;
import com.jnngl.client.protocol.*;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PacketHandler extends ChannelDuplexHandler {

    private static int freeId = 0;
    private static final Map<Short, Object> systems = new HashMap<>();
    private static final Map<String, Short> name2id = new HashMap<>();
    private static final Map<Short, Timer> renderTimers = new HashMap<>();

    private static Method TotalOS$renderFrame;
    private static Method TotalOS$processTouch;

    private static Object InteractType$LEFT_CLICK;
    private static Object InteractType$RIGHT_CLICK;

    private ChannelHandlerContext ctx;
    private final Client client;
    private boolean encryption = false;

    public PacketHandler(Client client) {
        this.client = client;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        ServerboundHandshakePacket c2s_handshake = new ServerboundHandshakePacket();
        c2s_handshake.protocolVersion = Client.getProtocolVersion();
        c2s_handshake.apiVersion = (short) client.getCoreApiVersion();
        ctx.writeAndFlush(c2s_handshake);
        super.channelActive(ctx);
    }

    private void handleHandshakeS2C(ClientboundHandshakePacket handshake_s2c) {
        if(encryption) {
            ctx.pipeline().addBefore("decrypt", "defrag", new PacketDefragmentation());
            ctx.pipeline().addBefore("encrypt", "prefixer", new PacketLengthPrefixer());
        } else {
            ctx.pipeline().addBefore("decoder", "defrag", new PacketDefragmentation());
            ctx.pipeline().addBefore("encoder", "prefixer", new PacketLengthPrefixer());
        }

        System.out.println(Localization.get(9)+handshake_s2c.serverName+Localization.get(10));
        ServerboundConnectPacket c2s_connect = new ServerboundConnectPacket();
        c2s_connect.token = client.getToken();
        ctx.writeAndFlush(c2s_connect);
    }

    private void handleDisconnectS2C(ClientboundDisconnectPacket disconnect_s2c) {
        System.out.println(Localization.get(11)+disconnect_s2c.reason);
        ctx.disconnect();
    }

    private void handleConnectionSuccessS2C(ClientboundConnectionSuccessPacket connectSuccess_s2c) {
        System.out.println(Localization.get(12)+connectSuccess_s2c.name);
    }

    private void handlePingS2C(ClientboundPingPacket s2c_ping) {
        ServerboundPongPacket c2s_pong = new ServerboundPongPacket();
        c2s_pong.payload = s2c_ping.payload;
        ctx.writeAndFlush(c2s_pong);
    }

    private void handlePaletteS2C(ClientboundPalettePacket s2c_palette) {
        System.out.println(Localization.get(13));
        Color[] palette = new Color[s2c_palette.palette.length];
        for(int i = 0; i < palette.length; i++)
            palette[i] = new Color(s2c_palette.palette[i]);
        MapColor.setPalette(palette);
        System.out.print(Localization.get(14));
        MapColor.cachePalette();
        System.out.println(Localization.get(15));
    }

    private void handleTouchS2C(ClientboundTouchPacket s2c_touch)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        if(!systems.containsKey(s2c_touch.id)) return;
        Object os = systems.get(s2c_touch.id);
        if (TotalOS$processTouch == null) {
            Class<?> InteractType = client.getCore()
                    .findClass("com.jnngl.totalcomputers.TotalComputers$InputInfo$InteractType");
            InteractType$LEFT_CLICK = InteractType.getField("LEFT_CLICK").get(null);
            InteractType$RIGHT_CLICK = InteractType.getField("RIGHT_CLICK").get(null);
            TotalOS$processTouch = os.getClass()
                    .getMethod("processTouch", int.class, int.class, InteractType, boolean.class);
        }
        TotalOS$processTouch.invoke(os, s2c_touch.x, s2c_touch.y,
                s2c_touch.type == ClientboundTouchPacket.LEFT_CLICK?
                        InteractType$LEFT_CLICK : InteractType$RIGHT_CLICK, s2c_touch.admin);
    }

    private void handleCreationRequestS2C(ClientboundCreationRequestPacket s2c_request)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ServerboundCreationStatusPacket c2s_status = new ServerboundCreationStatusPacket();
        c2s_status.status = ServerboundCreationStatusPacket.STATUS_OK;
        if(freeId > Short.MAX_VALUE) c2s_status.status = ServerboundCreationStatusPacket.STATUS_ERR;
        else c2s_status.id = (short)(freeId++);
        if(name2id.containsKey(s2c_request.name)) c2s_status.status = ServerboundCreationStatusPacket.STATUS_ERR;
        if(c2s_status.status == ServerboundCreationStatusPacket.STATUS_OK) {
            Object os = client.getCore().findClass("com.jnngl.totalcomputers.system.TotalOS")
                    .getMethod("createClientbound", int.class, int.class, String.class)
                    .invoke(null, s2c_request.width, s2c_request.height, s2c_request.name);
            systems.put(c2s_status.id, os);
            name2id.put(s2c_request.name, c2s_status.id);
            Timer renderTimer = new Timer();
            renderTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (TotalOS$renderFrame == null) {
                            TotalOS$renderFrame = os.getClass().getMethod("renderFrame");
                        }

                        BufferedImage screen = (BufferedImage) TotalOS$renderFrame.invoke(os);
                        byte[] raw = MapColor.toByteArray(screen);
                        byte[] sliced = new byte[raw.length];
                        for(int x = 0; x < screen.getWidth()/128; x++) {
                            for(int y = 0; y < screen.getHeight()/128; y++) {
                                int idx = y*(screen.getWidth()/128)+x;
                                for(int sx = 0; sx < 128; sx++) {
                                    for(int sy = 0; sy < 128; sy++) {
                                        sliced[idx*128*128+sy*128+sx] =
                                                raw[(y*128+sy)*s2c_request.width+x*128+sx];
                                    }
                                }
                            }
                        }
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        DeflaterOutputStream defl =
                                new DeflaterOutputStream(out, new Deflater(JZlib.Z_BEST_COMPRESSION));
                        defl.write(sliced);
                        defl.close();
                        byte[] data = out.toByteArray();
                        ServerboundFramePacket c2s_frame = new ServerboundFramePacket();
                        c2s_frame.id = c2s_status.id;
                        c2s_frame.compressedData = data;
                        ctx.writeAndFlush(c2s_frame);
                    } catch (IOException | ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                }
            }, 500, 1000/20);
            renderTimers.put(c2s_status.id, renderTimer);
        }
        ctx.writeAndFlush(c2s_status);
    }

    private void handleDestroyS2C(ClientboundDestroyPacket s2c_destroy)
            throws IllegalAccessException, NoSuchFieldException {
        if(!systems.containsKey(s2c_destroy.id)) return;
        Object os = systems.get(s2c_destroy.id);
        renderTimers.get(s2c_destroy.id).cancel();
        renderTimers.remove(s2c_destroy.id);
        name2id.remove((String)os.getClass().getField("name").get(os));
        systems.remove(s2c_destroy.id);
    }

    private void handleEncryptionS2C(ClientboundEncryptionPacket s2c_encryption)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
                    IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        System.out.println(Localization.get(16));
        Encryption encryption = new Encryption();
        encryption.decodePublicKeyRSA(s2c_encryption.publicKey);
        encryption.generateAES();
        ServerboundEncryptionPacket c2s_encryption = new ServerboundEncryptionPacket();
        c2s_encryption.secret = encryption.encryptRSA(encryption.aes.getEncoded());
        ctx.writeAndFlush(c2s_encryption);
        ctx.pipeline().addBefore("decoder", "decrypt", new PacketDecryptor(encryption));
        ctx.pipeline().addBefore("encoder", "encrypt", new PacketEncryptor(encryption));
        this.encryption = true;
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
        else if(msg instanceof ClientboundPalettePacket packet) handlePaletteS2C(packet);
        else if(msg instanceof ClientboundTouchPacket packet) handleTouchS2C(packet);
        else if(msg instanceof ClientboundEncryptionPacket packet) handleEncryptionS2C(packet);
        super.channelRead(ctx, msg);
    }



}
