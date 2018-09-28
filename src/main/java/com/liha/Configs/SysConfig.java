package com.liha.Configs;

import com.liha.entities.mysql.AccessRule;
import com.liha.entities.mysql.AccessSystem;
import com.liha.entities.mysql.RedisConfig;

import java.util.List;
import java.util.Map;

public class SysConfig {
    /**
     * 允许接入规则到对应的系统之间的映射
     */
    public static Map<AccessRule,AccessSystem> ruleMappingSys;

    /**
     * 接入的系统id到对应的Redis集群之间的映射
     */
    public static Map<Integer,List<RedisConfig>> sysMappingRedis;

    /**
     * Redis ip+port 与 对象之间的映射
     */
    public static Map<String,RedisConfig> addressMap;
}
