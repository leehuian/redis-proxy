package com.liha.entities.mysql;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 允许接入规则
 */
@Entity
@Table(name = "access_rule")
public class AccessRule {
    @Column(name="ruleid",updatable = false)
    @Id
    @GeneratedValue
    private int ruleID;

    @Column(name = "sysid")
    @NotNull
    private int sysID;

    @Column(name = "rule")
    @NotNull
    private String rule;

    @Column(name = "addtime")
    @NotNull
    private Date addTime;

    @Column(name = "updatetime")
    @NotNull
    private Date updateTime;

    @Column(name = "modiperson")
    @NotNull
    private String modiPerson;

    @Column(name = "lastchangeip")
    @NotNull
    private String lastChangeIP;

    @Column(name = "addperson")
    @NotNull
    private String addPerson;

    public int getRuleID() {
        return ruleID;
    }

    public void setRuleID(int ruleID) {
        this.ruleID = ruleID;
    }

    public int getSysID() {
        return sysID;
    }

    public void setSysID(int sysID) {
        this.sysID = sysID;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getModiPerson() {
        return modiPerson;
    }

    public void setModiPerson(String modiPerson) {
        this.modiPerson = modiPerson;
    }

    public String getLastChangeIP() {
        return lastChangeIP;
    }

    public void setLastChangeIP(String lastChangeIP) {
        this.lastChangeIP = lastChangeIP;
    }

    public String getAddPerson() {
        return addPerson;
    }

    public void setAddPerson(String addPerson) {
        this.addPerson = addPerson;
    }
}
