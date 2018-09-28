package com.liha.entities.mysql;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * 后端对应的redis配置
 */
@Table(name = "redis_config")
@Entity
public class RedisConfig {
    @Column(name="redisid",updatable = false)
    @Id
    @GeneratedValue
    private int redisID;

    @Column(name = "ip")
    @NotNull
    private String ip;

    @Column(name = "port")
    @NotNull
    private int port;

    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "area")
    @NotNull
    private String area;

    @Column(name = "iscluster")
    @NotNull
    private String isCluster;

    @Column(name = "maxmemory")
    @NotNull
    private int maxMemory;

    public int getRedisID() {
        return redisID;
    }

    public void setRedisID(int redisID) {
        this.redisID = redisID;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int prot) {
        this.port = prot;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getIsCluster() {
        return isCluster;
    }

    public void setIsCluster(String isCluster) {
        this.isCluster = isCluster;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }
}
