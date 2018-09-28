package com.liha.netty.sendredis;

import com.liha.netty.ChannelMapUtils;
import com.liha.netty.RedisMessageUtils;
import com.liha.netty.receiveclient.RuleRegexDispatcherHandler;
import io.netty.channel.*;
import io.netty.handler.codec.redis.RedisMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class SendToRedisHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendToRedisHandler.class);

    private ChannelMapUtils channelUtils = new ChannelMapUtils();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel inBoundChannel = channelUtils.getInChannel(ctx.channel());
        RuleRegexDispatcherHandler.closeOnFlush(inBoundChannel);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("redis 返回的信息：\t"+ctx.channel().id());
        RedisMessageUtils.printAggregatedRedisResponse((RedisMessage)msg);
        Channel outBoundChannel = ctx.channel();
        Channel inBoundChannel = channelUtils.getInChannel(outBoundChannel);

        //在读取完成后，释放outBoundChannel
        channelUtils.release(outBoundChannel);
        //向客户端写数据
        inBoundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(ExceptionUtils.getStackTrace(cause));
        RuleRegexDispatcherHandler.closeOnFlush(ctx.channel());
    }
}
