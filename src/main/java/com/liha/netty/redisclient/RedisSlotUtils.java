package com.liha.netty.redisclient;

import com.liha.netty.CRC16;
import io.netty.handler.codec.redis.ArrayRedisMessage;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.IntegerRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每个节点之间槽的分布情况
 */
public class RedisSlotUtils {

    /**
     * 每个redis节点的槽分布。格式：(ip:port):RedisSlot
     */
    private static Map<String,List<RedisSlot>> redisSlotMap;


    /**
     * 根据传入的可能的hostAndPort和key对应的槽的值，获得对应的 ip:port 字符串
     * @param hostAndPorts
     * @param slot
     * @return
     */
    public static String getHostAndPort(List<String> hostAndPorts,int slot) throws Exception {
        for (String hostAndPort : hostAndPorts) {
            for (RedisSlot redisSlot : redisSlotMap.get(hostAndPort)) {
                if (redisSlot.includeTheSlot(slot)){
                    return hostAndPort;
                }
            }
        }
        throw new Exception(String.format("根据传入的 hostAndPorts [%s] 和 slot[%s] ，未找到对应的 节点",hostAndPorts,slot));
    }

    public static String getHostAndPort(List<String> hostAndPorts,String key) throws Exception {
        return getHostAndPort(hostAndPorts,computeSlot(key));
    }

    /**
     * 根据输入的key，计算该key对应的槽
     * @param key
     * @return
     */
    private static int computeSlot(String key){
        int i = CRC16.getCRC16(key);
        return i%16384;
    }


    /**
     * 解析查询slot分布的返回信息。
     * @param msg
     */
    public static boolean parseSlot(RedisMessage msg) throws Exception {
        Map<String,List<RedisSlot>> map = new HashMap<>();
        if (!(msg instanceof ArrayRedisMessage)){
            throw new Exception("解析槽分布失败，传递的不是数组信息。");
        }
        //首先解析第一层，该层存储的粒度为每个槽范围的信息
        for (RedisMessage child : ((ArrayRedisMessage) msg).children()) {
            ArrayRedisMessage arrChild = (ArrayRedisMessage) child;
            //起始槽节点
            long start = ((IntegerRedisMessage)arrChild.children().get(0)).value();
            long end = ((IntegerRedisMessage)arrChild.children().get(1)).value();

            RedisSlot slot = new RedisSlot(start,end);
            //主从节点
            for (int i=2;i<arrChild.children().size();i++){
                ArrayRedisMessage node = (ArrayRedisMessage)arrChild.children().get(2);
                String host = ((FullBulkStringRedisMessage)node.children().get(0)).content().toString(CharsetUtil.UTF_8);
                int port = (int)((IntegerRedisMessage)node.children().get(1)).value();

                String hostAndPort = host+":"+port;
                //将槽分布存储
                if (!map.containsKey(hostAndPort)){
                    List<RedisSlot> list = new ArrayList<>();
                    list.add(slot);
                    map.put(hostAndPort,list);
                }else{
                    map.get(hostAndPort).add(slot);
                }
            }
        }
        //将解析结果存储到静态变量中
        redisSlotMap = map;
        return true;
    }

    private static class RedisSlot{
        private long start; //开始槽
        private long end; //结束槽

        public RedisSlot(long start,long end){
            this.start = start;
            this.end = end;
        }

        /**
         * 槽点是否在该范围中
         * @param slot
         * @return
         */
        public boolean includeTheSlot(int slot){
            return start<=slot && slot<=end;
        }

        public long getStart() {
            return start;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(long end) {
            this.end = end;
        }
    }

}
