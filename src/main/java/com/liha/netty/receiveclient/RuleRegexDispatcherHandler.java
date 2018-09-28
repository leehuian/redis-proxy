package com.liha.netty.receiveclient;

import com.liha.netty.AccessRuleRegex;
import com.liha.netty.ChannelMapUtils;
import com.liha.netty.RedisMessageUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

public class RuleRegexDispatcherHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleRegexDispatcherHandler.class);

    @Autowired
    private AccessRuleRegex accessRuleRegex = new AccessRuleRegex();

    @Autowired
    private ChannelMapUtils channelUtils = new ChannelMapUtils();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel inBoundChannel = ctx.channel();
        InetSocketAddress address = (InetSocketAddress) inBoundChannel.remoteAddress();
        String host = address.getHostString();
        List<String> redisClusterList = accessRuleRegex.enableConnectList(host);
        if (redisClusterList == null){
            LOGGER.error("Not allowed host [{}]",host);
            inBoundChannel.close();
        }else{
            LOGGER.info("new connection object is received. host="+host);
            inBoundChannel.read();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("接收的命令：\t" + ctx.channel().id());
        RedisMessageUtils.printAggregatedRedisResponse((RedisMessage)msg);

        Channel inBoundChannel = ctx.channel();
        SimpleChannelPool pool = channelUtils.getOutChannelPool(inBoundChannel);
        LOGGER.info("123");
        Future<Channel> f = pool.acquire();

        f.addListener((FutureListener<Channel>) f1 -> {
            if (f1.isSuccess()) {
                Channel outBoundChannel = f1.getNow();
                channelUtils.put(inBoundChannel,outBoundChannel,pool);
                LOGGER.info("向redis写数据的channelid=\t"+outBoundChannel.id());
                outBoundChannel.writeAndFlush(msg)
                        .addListener(new ChannelFutureListener(){
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                if (future.isSuccess()) {
                                    // was able to flush out data, start to read the next chunk
                                    LOGGER.info("写完成，读数据");
                                    ctx.channel().read();
                                } else {
                                    future.channel().close();
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(ExceptionUtils.getStackTrace(cause));
        ctx.close();
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    public static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
