package com.bancoxyz.core.repository;

import com.bancoxyz.core.model.CuentaAnualEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaAnualRepository extends JpaRepository<CuentaAnualEntity, Long> {
}