package com.liha.entities.mysql;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * redis集群与对应的系统之间的映射关系
 */
@Entity
@Table(name = "redis_system_relation")
public class RedisSystemRelation {

    @Column(name="relationid",updatable = false)
    @Id
    @GeneratedValue
    private int relationID;

    @Column(name = "sysid")
    @NotNull
    private int sysID;

    @Column(name = "redisid")
    @NotNull
    private int redisID;

    public int getRelationID() {
        return relationID;
    }

    public void setRelationID(int relationID) {
        this.relationID = relationID;
    }

    public int getSysID() {
        return sysID;
    }

    public void setSysID(int sysID) {
        this.sysID = sysID;
    }

    public int getRedisID() {
        return redisID;
    }

    public void setRedisID(int redisID) {
        this.redisID = redisID;
    }
}
