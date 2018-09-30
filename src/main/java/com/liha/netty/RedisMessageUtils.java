package com.liha.netty;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.redis.*;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.List;

public class RedisMessageUtils {
    public static String getAggregatedRedisResponse(RedisMessage msg) {
        if (msg instanceof SimpleStringRedisMessage) {
            return ((SimpleStringRedisMessage) msg).content();
        } else if (msg instanceof ErrorRedisMessage) {
            return ((ErrorRedisMessage) msg).content();
        } else if (msg instanceof IntegerRedisMessage) {
            return String.valueOf(((IntegerRedisMessage) msg).value());
        } else if (msg instanceof FullBulkStringRedisMessage) {
            return getString((FullBulkStringRedisMessage) msg);
        } else if (msg instanceof ArrayRedisMessage) {
            String result = "";
            for (RedisMessage child : ((ArrayRedisMessage) msg).children()) {
                result += getAggregatedRedisResponse(child)+System.lineSeparator();
            }
            return result;
        } else {
            throw new CodecException("unknown message type: " + msg);
        }
    }

    private static String getString(FullBulkStringRedisMessage msg) {
        if (msg.isNull()) {
            return "(null)";
        }
        return msg.content().toString(CharsetUtil.UTF_8);
    }

    public static RedisMessage createRedisMessage(String cmd, Channel ch){
        String[] commands = cmd.split("\\s+");
        List<RedisMessage> children = new ArrayList<RedisMessage>(commands.length);
        for (String cmdString : commands) {
            children.add(new FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(ch.alloc(), cmdString)));
        }
        RedisMessage request = new ArrayRedisMessage(children);
        return request;
    }
}
