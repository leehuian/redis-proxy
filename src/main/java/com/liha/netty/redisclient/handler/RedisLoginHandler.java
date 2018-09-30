package com.liha.netty.redisclient.handler;

import com.liha.netty.RedisMessageUtils;
import com.liha.netty.redisclient.LoginAndClusterUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisLoginHandler extends ChannelDuplexHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLoginHandler.class);

    private String password;
    public RedisLoginHandler(String pwd){
        this.password = pwd;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("create new channel_"+ctx.channel().id());
        String msg = "AUTH "+ password;
        RedisMessage request = RedisMessageUtils.createRedisMessage(msg,ctx.channel());
        ctx.writeAndFlush(request).addListeners(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()){
                    ctx.read();
                }else{
                    LOGGER.error("send Auth command failed [channelid_{}]",ctx.channel().id());
                    ctx.close();
                }
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof SimpleStringRedisMessage) {
            String result = ((SimpleStringRedisMessage) msg).content();
            //认证成功，移除认证handler
            if (result.equalsIgnoreCase("OK")){

                LOGGER.info("the channel {} login success",ctx.channel().id());
                LoginAndClusterUtils.setChannelLevel(ctx.channel(),1);
                ctx.channel().pipeline().remove("redisLoginHandler");

//                ctx.writeAndFlush(RedisMessageUtils.createRedisMessage("CLUSTER SLOTS", ctx.channel()))
//                        .addListeners(new GenericFutureListener<Future<? super Void>>() {
//                    @Override
//                    public void operationComplete(Future<? super Void> future) throws Exception {
//                        if (future.isSuccess()){
//                            ctx.channel().read();
//                        }
//                    }
//                });
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(ExceptionUtils.getStackTrace(cause));
        LoginAndClusterUtils.removeChannel(ctx.channel());
        ctx.close();
    }


}
