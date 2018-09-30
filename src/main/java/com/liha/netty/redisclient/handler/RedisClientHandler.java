package com.liha.netty.redisclient.handler;

import com.liha.netty.RedisMessageUtils;
import com.liha.netty.receiveclient.ClientToClusterUtils;
import com.liha.netty.receiveclient.InAndOutMapping;
import com.liha.netty.redisclient.Initialization;
import com.liha.netty.redisclient.KeyPoolFactory;
import com.liha.netty.redisclient.LoginAndClusterUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.redis.ArrayRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判定返回的命令是否是移动槽的命令，
        boolean isMoved = true;
        if (msg instanceof ArrayRedisMessage){
            ArrayRedisMessage message = (ArrayRedisMessage)msg;
            String cmd = RedisMessageUtils.getAggregatedRedisResponse(message.children().get(0));
            if (cmd.trim().equalsIgnoreCase("MOVED")){

                //添加节点槽分布处理的handler
                ctx.channel().pipeline().addBefore("redisClientHandler","clusterSlotHandler",new ClusterSlotHandler());
                ctx.writeAndFlush(RedisMessageUtils.createRedisMessage("CLUSTER SLOTS",ctx.channel()))
                        .addListeners(new GenericFutureListener<Future<? super Void>>() {
                            @Override
                            public void operationComplete(Future<? super Void> future) throws Exception {
                                if (future.isSuccess()){
                                    ctx.channel().read();
                                }
                            }
                        });
                isMoved = true;
            }
        }

        Channel outBoundChannel = ctx.channel();
        Channel inBoundChnnel = InAndOutMapping.getInBoundChannel(outBoundChannel);
        Initialization.poolFactory.releaseChannel(outBoundChannel);
        //如果是移动槽的命令，就先不归还outBoundChannel对象，等待处理完槽分布命令后再关闭
        if (!isMoved){
            InAndOutMapping.removeMapping(outBoundChannel);
        }
        //返回给客户端
        inBoundChnnel.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(ExceptionUtils.getStackTrace(cause));
        Initialization.poolFactory.releaseChannel(ctx.channel());
        LoginAndClusterUtils.removeChannel(ctx.channel());
        InAndOutMapping.removeMapping(ctx.channel());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LoginAndClusterUtils.removeChannel(ctx.channel());
        InAndOutMapping.removeMapping(ctx.channel());
        LOGGER.info("the channel_{} is Inactive",ctx.channel().id());
    }
}
