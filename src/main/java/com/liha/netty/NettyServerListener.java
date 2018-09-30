package com.liha.netty;

import com.liha.netty.receiveclient.RuleRegexDispatcherHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.redis.RedisArrayAggregator;
import io.netty.handler.codec.redis.RedisBulkStringAggregator;
import io.netty.handler.codec.redis.RedisDecoder;
import io.netty.handler.codec.redis.RedisEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * Netty服务启动监听器
 */
@Component("nettyServerListener")
public class NettyServerListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerListener.class);

    @Value("${netty.server.port}")
    private int port;

    @Value("${netty.server.port}")
    private int backlog;

    @Autowired
    private RuleRegexDispatcherHandler ruleRegexDispatcherHandler;

    ServerBootstrap strap = new ServerBootstrap();
    EventLoopGroup boss = new NioEventLoopGroup();
    EventLoopGroup work = new NioEventLoopGroup();

    /**
     * 关闭服务器方法
     */
    @PreDestroy
    public void close() {
        boss.shutdownGracefully();
        work.shutdownGracefully();
        LOGGER.info("netty Server closed success");
    }

    public void start() {
        try {
            strap.group(boss,work)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,backlog)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new RedisDecoder())
                                    .addLast(new RedisBulkStringAggregator())
                                    .addLast(new RedisArrayAggregator())
                                    .addLast(new RedisEncoder())
                                    .addLast(ruleRegexDispatcherHandler);
                        }
                    })
                    .childOption(ChannelOption.AUTO_READ, false);

            ChannelFuture future = strap.bind(port).sync();
        }catch (InterruptedException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
