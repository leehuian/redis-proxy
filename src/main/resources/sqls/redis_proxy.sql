/*
Navicat MySQL Data Transfer

Source Server         : 本地mysql
Source Server Version : 80012
Source Host           : localhost:3306
Source Database       : redis_proxy

Target Server Type    : MYSQL
Target Server Version : 80012
File Encoding         : 65001

Date: 2018-09-30 15:57:14
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for access_rule
-- ----------------------------
DROP TABLE IF EXISTS `access_rule`;
CREATE TABLE `access_rule` (
  `ruleid` int(8) NOT NULL AUTO_INCREMENT COMMENT '允许接入的规则id',
  `sysid` int(8) NOT NULL COMMENT '接入规则对应的系统id',
  `rule` varchar(255) NOT NULL COMMENT '规则内容，可以是完整的ip，正则表达式',
  `addtime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '添加事件',
  `updatetime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改事件',
  `modiperson` varchar(255) DEFAULT NULL COMMENT '修改人员',
  `lastchangeip` varchar(255) DEFAULT NULL COMMENT '最后进行修改的ip',
  `addperson` varchar(255) DEFAULT NULL COMMENT '添加规则的人员',
  PRIMARY KEY (`ruleid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='允许接入的规则';

-- ----------------------------
-- Table structure for access_system
-- ----------------------------
DROP TABLE IF EXISTS `access_system`;
CREATE TABLE `access_system` (
  `sysid` int(8) NOT NULL AUTO_INCREMENT COMMENT '在代理系统中的排序ID',
  `sysname` varchar(255) NOT NULL COMMENT '系统名称',
  `syscode` varchar(255) NOT NULL COMMENT '系统编码',
  `chinessname` varchar(255) DEFAULT NULL COMMENT '对应中文名',
  `sysmanager` varchar(255) DEFAULT NULL COMMENT '系统管理员',
  PRIMARY KEY (`sysid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='允许接入的系统';

-- ----------------------------
-- Table structure for redis_config
-- ----------------------------
DROP TABLE IF EXISTS `redis_config`;
CREATE TABLE `redis_config` (
  `redisid` int(8) NOT NULL AUTO_INCREMENT COMMENT 'redis ID',
  `ip` varchar(255) NOT NULL COMMENT 'redis 节点ip',
  `port` int(6) NOT NULL COMMENT 'redis 节点 端口',
  `password` varchar(255) NOT NULL COMMENT '节点密码',
  `area` varchar(2) NOT NULL COMMENT '节点所在区域：SZ/SH',
  `iscluster` varchar(1) NOT NULL COMMENT '是否是集群模式 (1：是；0：不是)',
  `maxmemory` int(8) NOT NULL COMMENT '最大可用内存（单位：M）',
  PRIMARY KEY (`redisid`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='redis节点配置信息';

-- ----------------------------
-- Table structure for redis_system_relation
-- ----------------------------
DROP TABLE IF EXISTS `redis_system_relation`;
CREATE TABLE `redis_system_relation` (
  `relationid` int(8) NOT NULL COMMENT '关系ID',
  `sysid` int(8) NOT NULL COMMENT '系统id',
  `redisid` int(8) NOT NULL COMMENT 'redis id',
  PRIMARY KEY (`relationid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接入系统与等级的redis节点之间的对应关系';
