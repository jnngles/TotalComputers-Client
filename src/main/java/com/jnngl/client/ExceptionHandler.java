package com.jnngl.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println(Localization.get(18)+cause.getClass().getName()+": "+cause.getMessage());
        System.err.println(Localization.get(19));
        System.err.println(Localization.get(20));
        System.err.println(Localization.get(21));
        ctx.disconnect();
    }
}
