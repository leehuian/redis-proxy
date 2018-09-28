package com.liha.entities.mysql;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * 接入系统
 */
@Entity
@Table(name = "access_system")
public class AccessSystem {

    @Column(name="sysid",updatable = false)
    @Id
    @GeneratedValue
    private int sysID;

    @Column(name = "sysname")
    @NotNull
    private String sysName;

    @Column(name = "syscode")
    @NotNull
    private String sysCode;

    @Column(name = "chinessname")
    private String chinessName;

    @Column(name = "sysmanager")
    private String sysManager;

    public int getSysID() {
        return sysID;
    }

    public void setSysID(int sysID) {
        this.sysID = sysID;
    }

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public String getSysCode() {
        return sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public String getChinessName() {
        return chinessName;
    }

    public void setChinessName(String chinessName) {
        this.chinessName = chinessName;
    }

    public String getSysManager() {
        return sysManager;
    }

    public void setSysManager(String sysManager) {
        this.sysManager = sysManager;
    }
}
