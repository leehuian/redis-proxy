package com.liha.entities.repository;

import com.liha.entities.mysql.RedisConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RedisConfigRepository extends JpaRepository<RedisConfig,Integer> {
    List<RedisConfig> findAll();
    List<RedisConfig> findByIp(String ip);
    List<RedisConfig> findAllByArea(String area);

    RedisConfig findByIpAndPort(String ip,int port);
}
