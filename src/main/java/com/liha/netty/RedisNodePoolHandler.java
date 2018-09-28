package com.liha.netty;

import com.liha.Configs.SysConfig;
import com.liha.netty.sendredis.SendToRedisHandler;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.redis.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class RedisNodePoolHandler implements ChannelPoolHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisNodePoolHandler.class);

    @Override
    public void channelReleased(Channel ch) throws Exception {
        System.out.println("channelReleased. Channel ID: " + ch.id());
    }

    @Override
    public void channelAcquired(Channel ch) throws Exception {
        System.out.println("channelAcquired. Channel ID: " + ch.id());
        buildLoginAuth(ch);
    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        SocketChannel channel = (SocketChannel)ch;
        channel.config().setKeepAlive(true);
        channel.config().setTcpNoDelay(true);
        channel.pipeline()
                .addLast(new RedisDecoder())
                .addLast(new RedisBulkStringAggregator())
                .addLast(new RedisArrayAggregator())
                .addLast(new RedisEncoder())
                .addLast(new SendToRedisHandler());

        LOGGER.info("remotAddress=\t"+channel.remoteAddress());
        LOGGER.info("create new channel . Channel ID: " + ch.id());
    }

    private static void buildLoginAuth(Channel ch) throws Exception {
        InetSocketAddress address = (InetSocketAddress)ch.remoteAddress();
        String hostAndPort = address.getHostString()+":"+address.getPort();

        if (!SysConfig.addressMap.containsKey(hostAndPort)){
            throw new Exception("Can't find the redisConfig about "+hostAndPort);
        }

        String password = SysConfig.addressMap.get(hostAndPort).getPassword();
        String cmdLine = "AUTH "+ password;

        String[] commands = cmdLine.split("\\s+");
        List<RedisMessage> children = new ArrayList<RedisMessage>(commands.length);
        for (String cmdString : commands) {
            children.add(new FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(ch.alloc(), cmdString)));
        }
        RedisMessage request = new ArrayRedisMessage(children);
        ch.writeAndFlush(request).addListener(new ChannelFutureListener(){
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    LOGGER.info("登录认证写成功\t"+ch.id());
                    //ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
    }
}
