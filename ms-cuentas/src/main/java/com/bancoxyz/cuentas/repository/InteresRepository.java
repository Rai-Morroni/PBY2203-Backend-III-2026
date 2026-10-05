package com.bancoxyz.cuentas.repository;

import com.bancoxyz.cuentas.model.InteresEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteresRepository extends JpaRepository<InteresEntity, Long> {
}