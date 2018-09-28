package com.liha.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class RedisNodePoolClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisNodePoolClient.class);

    private ChannelPoolMap<String, SimpleChannelPool> poolMap;

    private void build(){
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        try {
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.AUTO_READ, false)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE,true);

            poolMap = new AbstractChannelPoolMap<String, SimpleChannelPool>() {
                @Override
                protected SimpleChannelPool newPool(String key) {

                    String[] params = key.split(":");
                    String host = params[0];
                    int port = Integer.parseInt(params[1]);
                    InetSocketAddress address = new InetSocketAddress(host,port);
                    System.out.println("2222222222");
                    return new FixedChannelPool(b.remoteAddress(address), new RedisNodePoolHandler(), 2);
                }
            };
        }catch (Exception e){
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    /**
     * 获取连接
     * @param key
     * @return
     */
    public SimpleChannelPool get(String key){
        if (poolMap == null){
            build();
        }
        return poolMap.get(key);
    }

    public static void main(String[] args) {

    }
}
