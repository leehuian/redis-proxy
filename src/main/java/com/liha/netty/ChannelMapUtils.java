package com.liha.netty;

import io.netty.channel.Channel;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelMapUtils {
    private volatile static Map<Channel,Channel> inToOutMap = new HashMap<>();

    private volatile static Map<Channel,Channel> outToinMap = new HashMap<>();

    private volatile static Map<Channel,SimpleChannelPool> outToPoolMap = new HashMap<>();

    private volatile static AccessRuleRegex accessRuleRegex = new AccessRuleRegex();

    private volatile static RedisNodePoolClient redisPool = new RedisNodePoolClient();


    /**
     * 根据传入的 channel ，查找对应的到目标服务器的channel
     * @param inBoundChannel
     * @return
     * @throws Exception
     */
    public Channel getOutChannel(Channel inBoundChannel) throws Exception {
        if (inToOutMap.containsKey(inBoundChannel)){
            return inToOutMap.get(inBoundChannel);
        }
        InetSocketAddress add = (InetSocketAddress)inBoundChannel.remoteAddress();
        throw new Exception("can't find outBoundChannel by the inBoundChannel[{"+add.getHostString()+"}:{"+add.getPort()+"}]");
    }

    /**
     * 根据传入的到目标服务器的channel，找到对应的连接客户端的channel
     * @param outBoundChannel
     * @return
     * @throws Exception
     */
    public Channel getInChannel(Channel outBoundChannel) throws Exception{
        if (outToinMap.containsKey(outBoundChannel)){
            return outToinMap.get(outBoundChannel);
        }
        InetSocketAddress add = (InetSocketAddress)outBoundChannel.remoteAddress();
        throw new Exception("can't find inBoundChannel by the outBoundChannel[{"+add.getHostString()+"}:{"+add.getPort()+"}]");
    }

    /**
     * 根据传入的到目标服务器的channel，释放对应的映射关系。
     * @param outBoundChannel
     * @throws Exception
     */
    public void release(Channel outBoundChannel) throws Exception {
        Channel inBoundChannel = getInChannel(outBoundChannel);
        outToinMap.remove(outBoundChannel);
        inToOutMap.remove(inBoundChannel);

        //释放资源
        outToPoolMap.remove(outBoundChannel).release(outBoundChannel);
    }

    /**
     * 存储映射关系
     * @param inBoundChannel 客户端请求的channel
     * @param outBoundChannel 请求目标服务器的channel
     * @param pool 连接池对象
     */
    public void put(Channel inBoundChannel,Channel outBoundChannel,SimpleChannelPool pool){
        outToinMap.put(outBoundChannel,inBoundChannel);
        inToOutMap.put(inBoundChannel,outBoundChannel);
        outToPoolMap.put(outBoundChannel,pool);
    }

    /**
     * 根据客户端远程连接对象，获取链接对象Future<Channel>
     * @param inBoundChannel
     * @return
     */
    public SimpleChannelPool getOutChannelPool(Channel inBoundChannel){
        InetSocketAddress address = (InetSocketAddress) inBoundChannel.remoteAddress();
        String host = address.getHostString();
        List<String> redisClusterList = accessRuleRegex.enableConnectList(host);

        int hashCode = host.hashCode();
        int len = redisClusterList.size();
        String hostAndPort = redisClusterList.get(hashCode%len);
        SimpleChannelPool pool = redisPool.get(hostAndPort);
        return pool;
    }
}
