package com.liha.Configs;

import com.liha.entities.mysql.AccessRule;
import com.liha.entities.mysql.AccessSystem;
import com.liha.entities.mysql.RedisConfig;
import com.liha.entities.mysql.RedisSystemRelation;
import com.liha.entities.repository.AccessRuleRepository;
import com.liha.entities.repository.AccessSystemRepository;
import com.liha.entities.repository.RedisConfigRepository;
import com.liha.entities.repository.RedisSystemRelationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("sysConfig")
public class SysConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SysConfig.class);

    /**
     * 允许接入规则到对应的系统之间的映射
     */
    private volatile static Map<AccessRule,AccessSystem> ruleMappingSys;

    /**
     * 接入的系统id到对应的Redis集群之间的映射
     */
    private volatile static Map<Integer,List<RedisConfig>> sysMappingRedis;

    /**
     * Redis ip+port 与 对象之间的映射
     */
    private volatile static Map<String,RedisConfig> addressMap;


    @Autowired
    private AccessRuleRepository accessRuleRepository;

    @Autowired
    private AccessSystemRepository accessSystemRepository;

    @Autowired
    private RedisConfigRepository redisConfigRepository;

    @Autowired
    private RedisSystemRelationRepository redisSystemRelationRepository;

    @PostConstruct
    private void initialize(){
        //查询所有的配置数据
        List<AccessSystem> sysList = accessSystemRepository.findAll();
        List<AccessRule> ruleList = accessRuleRepository.findAll();
        List<RedisConfig> configList = redisConfigRepository.findAll();
        List<RedisSystemRelation> relationList = redisSystemRelationRepository.findAll();

        /**
         * List -> Map
         * 需要注意的是：
         * toMap 如果集合对象有重复的key，会报错Duplicate key ....
         *  AccessSystem1,AccessSystem2 的id都为1。
         *  可以用 (k1,k2)->k1 来设置，如果有重复的key,则保留key1,舍弃key2
         */
        Map<Integer,AccessSystem> systemMap = sysList.stream().collect(Collectors.toMap(AccessSystem::getSysID, a->a,(k1, k2)->k1));
        Map<Integer,RedisConfig> redisMap = configList.stream().collect(Collectors.toMap(RedisConfig::getRedisID,a->a,(k1,k2)->k1));

        Map<String,RedisConfig> addressMap = new HashMap<>();
        redisMap.forEach((k,v)->{
            addressMap.put(v.getIp()+":"+v.getPort(),v);
        });
        SysConfig.addressMap = addressMap;

        Map<AccessRule,AccessSystem> ruleMappingSys = new HashMap<>();
        for (AccessRule rule : ruleList) {
            int sysid = rule.getSysID();
            if (systemMap.containsKey(sysid)){
                ruleMappingSys.put(rule,systemMap.get(sysid));
            }else{
                LOGGER.error("can't find the AccessSystem by id [{}] from AccessRule",sysid);
            }
        }

        Map<Integer,List<RedisConfig>> sysMappingRedis = new HashMap<>();
        for (RedisSystemRelation relation : relationList) {
            int sysID = relation.getSysID();
            int redisID = relation.getRedisID();
            if (!systemMap.containsKey(sysID)){
                LOGGER.error("can't find the AccessSystem by id [{}] from RedisSystemRelation",sysID);
                continue;
            }
            if (!redisMap.containsKey(redisID)){
                LOGGER.error("can't find the RedisConfig by id [{}] from RedisSystemRelation",redisID);
                continue;
            }
            RedisConfig redis = redisMap.get(redisID);
            if (sysMappingRedis.containsKey(sysID)){
                sysMappingRedis.get(sysID).add(redis);
            }else{
                List<RedisConfig> list = new ArrayList<>();
                list.add(redis);
                sysMappingRedis.put(sysID,list);
            }
        }

        SysConfig.ruleMappingSys = ruleMappingSys;
        SysConfig.sysMappingRedis = sysMappingRedis;
    }

    /**
     * 获得接入规则到对应的系统之间的映射
     * @return
     */
    public Map<AccessRule, AccessSystem> getRuleMappingSys() {
        return ruleMappingSys;
    }

    /**
     * 接入的系统id到对应的Redis集群之间的映射
     * @return
     */
    public Map<Integer, List<RedisConfig>> getSysMappingRedis() {
        return sysMappingRedis;
    }

    /**
     * Redis ip+port 与 对象之间的映射
     * @return
     */
    public Map<String, RedisConfig> getAddressMap() {
        return addressMap;
    }
}
