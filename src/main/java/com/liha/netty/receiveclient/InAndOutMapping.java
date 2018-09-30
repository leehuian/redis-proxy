package com.liha.netty.receiveclient;

import com.liha.netty.redisclient.Initialization;
import io.netty.channel.Channel;
import org.apache.coyote.OutputBuffer;

import java.util.HashMap;
import java.util.Map;

public class InAndOutMapping {
    private volatile static Map<Channel,Channel> outToinMap = new HashMap<>();

    public static void putMapping(Channel outBoundChannel,Channel inBoundChannel){
        outToinMap.put(outBoundChannel,inBoundChannel);
    }

    public static Channel getInBoundChannel(Channel outBoundChannel){
        return outToinMap.get(outBoundChannel);
    }

    public static void removeMapping(Channel outBoundChannel){
        outToinMap.remove(outBoundChannel);
    }

    public static void removeAllInMapping(Channel inBoundChannel){
        for (Channel out : outToinMap.keySet()) {
            if (outToinMap.get(out)==inBoundChannel){
                outToinMap.remove(out);
                //释放占用的连接
//                Initialization.poolFactory.releaseChannel(out);
            }
        }
    }
}
