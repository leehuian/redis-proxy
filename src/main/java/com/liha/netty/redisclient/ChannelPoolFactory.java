package com.liha.netty.redisclient;

import com.liha.Configs.SysConfig;
import com.liha.entities.mysql.RedisConfig;
import com.liha.netty.redisclient.handler.ClusterSlotHandler;
import com.liha.netty.redisclient.handler.RedisClientHandler;
import com.liha.netty.redisclient.handler.RedisLoginHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.redis.*;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("channelPoolFactory")
public class ChannelPoolFactory extends BaseKeyedPooledObjectFactory<String, Channel> {

    @Autowired
    private SysConfig sysConfig;

    /**
     * 创建对象
     * @param s
     * @return
     * @throws Exception
     */
    @Override
    public Channel create(String s) throws Exception {
        Map<String, RedisConfig> redisMap = sysConfig.getAddressMap();
        if (!redisMap.containsKey(s)){
            throw new Exception("can't find the redisConfig by "+s);
        }
        RedisConfig redisConfig = sysConfig.getAddressMap().get(s);

        return createChannel(redisConfig);
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<Channel>(channel);
    }


    /**
     * 销毁对象
     * @param key
     * @param p
     * @throws Exception
     */
    @Override
    public void destroyObject(String key, PooledObject<Channel> p) throws Exception {
        EventLoopGroup group = p.getObject().eventLoop().parent();
        p.getObject().closeFuture();
        group.shutdownGracefully();
    }

    /**
     * 验证对象是否有效
     * @param key
     * @param p
     * @return
     */
    @Override
    public boolean validateObject(String key, PooledObject<Channel> p) {
        return p.getObject().isActive();
    }

    /**
     * 用于从pool中借出去之前检测该对象是否处于钝化状态
     * @param key
     * @param p
     * @throws Exception
     */
    @Override
    public void activateObject(String key, PooledObject<Channel> p) throws Exception {
        super.activateObject(key, p);
    }

    /**
     * 当对象被归还到池中之前执行
     * @param key
     * @param p
     * @throws Exception
     */
    @Override
    public void passivateObject(String key, PooledObject<Channel> p) throws Exception {
        super.passivateObject(key, p);
    }


    private Channel createChannel(RedisConfig config) throws InterruptedException {
        String host = config.getIp();
        int port = config.getPort();
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.AUTO_READ,false)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new RedisDecoder());
                        p.addLast(new RedisBulkStringAggregator());
                        p.addLast(new RedisArrayAggregator());
                        p.addLast(new RedisEncoder());
                        p.addLast("redisLoginHandler",new RedisLoginHandler(config.getPassword()));
                        //p.addLast("clusterSlotHandler",new ClusterSlotHandler());
                        p.addLast("redisClientHandler",new RedisClientHandler());
                    }
                });

        // 绑定连接地址
        Channel ch = b.connect(host, port).sync().channel();
        return ch;
    }
}
