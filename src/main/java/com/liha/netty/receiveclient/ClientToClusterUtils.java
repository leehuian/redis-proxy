package com.liha.netty.receiveclient;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存储每个客户端对应的集群信息。
 */
@Component("clientToClusterUtils")
public class ClientToClusterUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientToClusterUtils.class);
    /**
     * 接入的channel与对应的redisCluster 的 ip:port集合之间的映射
     */
    private static volatile Map<Channel,List<String>> map = new HashMap<>();


    /**
     * 存放映射关系
     * @param channel
     * @param list
     */
    public static void putRelation(Channel channel,List<String> list){
        map.put(channel,list);
    }


    /**
     * 移除映射关系
     * @param channel
     */
    public static void removeRelation(Channel channel){
        if (map.containsKey(channel))
            map.remove(channel);
    }

    /**
     * 获取channel对应的redisCluster节点字符串集合
     * @param channel
     * @return
     */
    public static List<String> getCluster(Channel channel){
        return map.get(channel);
    }


    @Scheduled(fixedDelay = 10*60*1000)
    private void filterUnActiveChannel(){
        for (Channel channel : map.keySet()) {
            if (!channel.isActive()){
                map.remove(channel);
            }
        }
        LOGGER.info("清除已关闭链接映射成功");
    }
}
