package com.bancoxyz.batch_legacy.repository;

import com.bancoxyz.batch_legacy.model.CuentaAnualEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaAnualRepository extends JpaRepository<CuentaAnualEntity, Long> {
}