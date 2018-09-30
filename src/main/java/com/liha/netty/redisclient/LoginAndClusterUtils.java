package com.liha.netty.redisclient;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录、计算slot的辅助类
 */
@Component("loginAndClusterUtils")
public class LoginAndClusterUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginAndClusterUtils.class);
    /**
     * 0:表示未登录，未计算
     * 1：已登录
     * 2：已计算slot
     */
    private volatile static Map<Channel,Integer> outChannelLevel = new HashMap<>();

    public static int getLevel(Channel channel){
        if (outChannelLevel.containsKey(channel)){
            return outChannelLevel.get(channel);
        }else{
            outChannelLevel.put(channel,0);
            return 0;
        }
    }

    public static void removeChannel(Channel outBoundChannel){
        outChannelLevel.remove(outBoundChannel);
    }

    /**
     * 0:未登录认证
     * 1:已登录认证
     * 2:已计算slot
     * @param outBoundChannel
     * @param level
     */
    public static void setChannelLevel(Channel outBoundChannel,int level){
        outChannelLevel.put(outBoundChannel,level);
    }

    /**
     * 定时任务清理所有已经关闭的channel
     */
    @Scheduled(fixedDelay = 10*60*1000)
    private void filterChannelLevel(){
        for (Channel channel : outChannelLevel.keySet()) {
            if (channel.isActive() && channel.isOpen()){
                //do notings
            }else{
                outChannelLevel.remove(channel);
            }
        }
        LOGGER.info("timed task to clear loginAndClusterUtils success");
    }
}
