package com.liha.netty.redisclient.handler;

import com.liha.netty.receiveclient.InAndOutMapping;
import com.liha.netty.redisclient.Initialization;
import com.liha.netty.redisclient.LoginAndClusterUtils;
import com.liha.netty.redisclient.RedisSlotUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.redis.RedisMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterSlotHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterSlotHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RedisMessage message = (RedisMessage) msg;
        RedisSlotUtils.parseSlot(message);
        LoginAndClusterUtils.setChannelLevel(ctx.channel(),2);

        Initialization.poolFactory.releaseChannel(ctx.channel());
        InAndOutMapping.removeMapping(ctx.channel());

        ctx.channel().pipeline().remove("clusterSlotHandler");
        LOGGER.info("parse cluster slot succes. the channel_"+ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(ExceptionUtils.getStackTrace(cause));
        LoginAndClusterUtils.removeChannel(ctx.channel());
        InAndOutMapping.removeMapping(ctx.channel());
        ctx.close();
    }
}
