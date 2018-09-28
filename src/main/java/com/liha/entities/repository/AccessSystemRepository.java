package com.liha.entities.repository;

import com.liha.entities.mysql.AccessSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessSystemRepository extends JpaRepository<AccessSystem,Integer> {
    List<AccessSystem> findAll();

    AccessSystem findBySysCode(String sysCode);
}
