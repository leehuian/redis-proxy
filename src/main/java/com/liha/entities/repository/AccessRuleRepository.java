package com.liha.entities.repository;

import com.liha.entities.mysql.AccessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessRuleRepository extends JpaRepository<AccessRule, Integer> {
    AccessRule findAllByRuleID(int ruleid);
    List<AccessRule> findAll();
    List<AccessRule> findAllBySysID(int sysID);

    List<AccessRule> findAllByRule(String rule);
}
