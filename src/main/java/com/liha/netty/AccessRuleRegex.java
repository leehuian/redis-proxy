package com.liha.netty;

import com.liha.Configs.SysConfig;
import com.liha.entities.mysql.AccessRule;
import com.liha.entities.mysql.AccessSystem;
import com.liha.entities.mysql.RedisConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AccessRuleRegex {
    private static Map<Pattern,List<String>> regexMap;
    private static List<Pattern> regexList;

    //存储每次解析出来的ip对应的数据。
    private static Map<String,List<String>> ipToRedisMap = new HashMap<>();

    static {
        regexMap = new HashMap<>();
        regexList = new ArrayList<>();
        Map<AccessRule,AccessSystem> ruleMappingSys = SysConfig.ruleMappingSys;
        ruleMappingSys.forEach((rule,sys)->{

            //编译允许接入的规则的正则表达式
            String regEx = rule.getRule();
            Pattern pattern = Pattern.compile(regEx);

            //规则对应的redis cluster
            int sysid = rule.getSysID();
            List<RedisConfig> list = SysConfig.sysMappingRedis.get(sysid);
            List<String> valueList = new ArrayList<>();
            list.forEach(v->valueList.add(v.getIp()+":"+v.getPort()));

            regexList.add(pattern);
            regexMap.put(pattern,valueList);
        });
    }

    /**
     * 根据IP，解析出该IP对应的后端redis集群，并返回这些集群的节点[ip:prot] 集合
     * @param ip
     * @return
     */
    public List<String> enableConnectList(String ip){
        if (ipToRedisMap.containsKey(ip)){
            return ipToRedisMap.get(ip);
        }
        for (Pattern pattern : regexList) {
            //如果符合允许接入规则
            if (pattern.matcher(ip).matches()){
                ipToRedisMap.put(ip,regexMap.get(pattern));
                return regexMap.get(pattern);
            }
        }
        return null;
    }
}
