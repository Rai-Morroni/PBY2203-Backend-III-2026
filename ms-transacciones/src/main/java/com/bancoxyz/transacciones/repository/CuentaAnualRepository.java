package com.bancoxyz.transacciones.repository;

import com.bancoxyz.transacciones.model.CuentaAnualEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaAnualRepository extends JpaRepository<CuentaAnualEntity, Long> {
}