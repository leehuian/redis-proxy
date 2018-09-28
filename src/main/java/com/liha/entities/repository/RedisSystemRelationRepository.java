package com.liha.entities.repository;

import com.liha.entities.mysql.RedisSystemRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RedisSystemRelationRepository extends JpaRepository<RedisSystemRelation,Integer> {
    List<RedisSystemRelation> findAll();

    List<RedisSystemRelation> findAllBySysID(int sysID);

    List<RedisSystemRelation> findAllByRedisID(int redisID);
}
