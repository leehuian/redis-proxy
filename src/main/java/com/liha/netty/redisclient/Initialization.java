package com.liha.netty.redisclient;

import com.liha.Configs.SysConfig;
import com.liha.entities.mysql.RedisConfig;
import com.liha.netty.RedisMessageUtils;
import com.liha.netty.redisclient.handler.ClusterSlotHandler;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("initialization")
public class Initialization {

    public static KeyPoolFactory poolFactory;

    @Autowired
    private KeyPoolFactory keyPoolFactory;

    @Autowired
    private SysConfig sysConfig;

    public void initAll() throws Exception {
        System.out.println("start initAll()...");
        keyPoolFactory.initFactory();
        initSlot();
        poolFactory = keyPoolFactory;
    }

    private void initSlot() throws Exception {
        Map<Integer,List<RedisConfig>> map = sysConfig.getSysMappingRedis();
        for (Integer sysid : map.keySet()) {
            List<RedisConfig> list = map.get(sysid);
            RedisConfig redis = list.get(0);
            String hostAndPort = redis.getIp()+":"+redis.getPort();
            Channel channel = keyPoolFactory.getChannel(hostAndPort);

            //添加节点槽分布处理的handler
            channel.pipeline().addBefore("redisClientHandler","clusterSlotHandler",new ClusterSlotHandler());
            channel.writeAndFlush(RedisMessageUtils.createRedisMessage("CLUSTER SLOTS",channel))
                    .addListeners(new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            if (future.isSuccess()){
                                channel.read();
                            }
                        }
                    });
        }
    }
}
