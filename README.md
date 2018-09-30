# redis-proxy
一个基于netty+springboot实现的redis代理

###实现功能

- 一个代理服务器对接多个系统
- 客户端连接代理服务器，可使用单节点的形式，代理服务器后端对接redis 3.0 集群
- 代理服务器自动发现slot迁移，并调整



### 运行环境

- Netty 4.1.25.Final
- Mysql 8.1
- Spring Boot 1.5



### 启动运行

1. 创建mysql，定义数据
2. 运行application
3. 客户端连接

#### 效果图如下：

```java
2018-09-30 16:08:39.926  INFO 13292 --- [ntLoopGroup-3-1] c.l.n.r.RuleRegexDispatcherHandler       : new connection object is received. host=99.6.150.145
2018-09-30 16:08:39.974  INFO 13292 --- [ntLoopGroup-5-1] c.l.n.r.handler.RedisLoginHandler        : create new channel_7ae62d5f
2018-09-30 16:08:40.012  INFO 13292 --- [ntLoopGroup-5-1] c.l.n.r.handler.RedisLoginHandler        : the channel 7ae62d5f login success
```

客户端代码：

```java
Jedis redis = new Jedis("99.6.150.145",8010);
        try {
            String str = redis.get("lihuian");
            System.out.println(str);
        }catch (Exception e){
            e.printStackTrace();
        }
```



