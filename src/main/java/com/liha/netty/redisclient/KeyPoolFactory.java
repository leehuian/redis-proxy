package com.liha.netty.redisclient;

import io.netty.channel.Channel;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component("keyPoolFactory")
public class KeyPoolFactory {

    /**
     * 对象池
     */
    private volatile static GenericKeyedObjectPool<String, Channel> pool;

    /**
     * 对象池的参数设置
     */
    private volatile static GenericKeyedObjectPoolConfig config;


    @Autowired
    private ChannelPoolFactory channelPoolFactory;

    @Value("${redisChannel.pool.maxIdle}")
    private int maxIdle;

    @Value("${redisChannel.pool.minIdle}")
    private int minIdle;

    @Value("${redisChannel.pool.maxWaitMillis}")
    private int maxWaitMillis;

    @Value("${redisChannel.pool.maxTotal}")
    private int maxTotal;

    public void initFactory(){
        config = new GenericKeyedObjectPoolConfig();
        config.setMaxIdlePerKey(maxIdle);
        config.setMinIdlePerKey(minIdle);
        config.setMaxWaitMillis(maxWaitMillis);
        config.setMaxTotalPerKey(maxTotal);

        initPool();
    }

    /**
     * 从对象池中获取对象
     * @param key
     * @return
     * @throws Exception
     */
    public Channel getChannel (String key) throws Exception {
        if (pool == null) {
            initPool();
        }
        Channel channel = pool.borrowObject(key);
        //对象取出来之间，必须先判断是否经过了认证和计算slot分布，如果没有，需要执行完毕这两个步骤之后再返回给对象
        int level = LoginAndClusterUtils.getLevel(channel);
        int count = 0;
        while (level==0){
            Thread.sleep(10);
            level = LoginAndClusterUtils.getLevel(channel);
            count++;
            if (count==300){
                throw new Exception("执行登录、slot计算超时");
            }
        }

        return channel;
    }

    /**
     * 归还对象
     * @param channel
     */
    public void releaseChannel ( Channel channel) {
        if (pool == null) {
            initPool();
        }
        InetSocketAddress address = (InetSocketAddress)channel.remoteAddress();
        String key = address.getHostString()+":"+address.getPort();
        pool.returnObject(key , channel);
    }

    /**
     * 关闭对象池
     */
    public void close () {
        if (pool !=null && !pool.isClosed()) {
            pool.close();
            pool = null;
        }
    }
    /**
     * 初始化对象池
     */
    private void initPool () {
        if (pool != null) return;
        pool = new GenericKeyedObjectPool<String, Channel>(channelPoolFactory, config);
    }

}
