package com.liha.netty.receiveclient;

import com.liha.netty.AccessRuleRegex;
import com.liha.netty.RedisMessageUtils;
import com.liha.netty.redisclient.KeyPoolFactory;
import com.liha.netty.redisclient.RedisSlotUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.handler.codec.redis.ArrayRedisMessage;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

@ChannelHandler.Sharable
@Component("ruleRegexDispatcherHandler")
public class RuleRegexDispatcherHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleRegexDispatcherHandler.class);

    @Autowired
    private AccessRuleRegex accessRuleRegex;

    @Autowired
    private KeyPoolFactory keyPoolFactory;


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
            //将映射关系存储
            ClientToClusterUtils.putRelation(inBoundChannel,redisClusterList);
            inBoundChannel.read();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel inBoundChannel = ctx.channel();
        ArrayRedisMessage message = (ArrayRedisMessage)msg;
        String key = ((FullBulkStringRedisMessage)message.children().get(1)).content().toString(CharsetUtil.UTF_8);
        List<String> cluster = ClientToClusterUtils.getCluster(inBoundChannel);
        String hostAndPort = RedisSlotUtils.getHostAndPort(cluster,key);
        Channel outBoundChannel = keyPoolFactory.getChannel(hostAndPort);
        InAndOutMapping.putMapping(outBoundChannel,inBoundChannel);

        //接收下一个客户端请求
        inBoundChannel.read();

        //转发请求
        outBoundChannel.writeAndFlush(msg).addListeners(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()){
                    //接收回复信息
                    outBoundChannel.read();
                }
            }
        });
    }


    /**
     * 关闭连接事件
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientToClusterUtils.removeRelation(ctx.channel());
        InAndOutMapping.removeAllInMapping(ctx.channel());
        //向下传递关闭连接事件
//        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //LOGGER.error(ExceptionUtils.getStackTrace(cause));
        LOGGER.error(cause.getMessage());
        //移除映射关系
        ClientToClusterUtils.removeRelation(ctx.channel());
        InAndOutMapping.removeAllInMapping(ctx.channel());
        ctx.close();
    }
}
