package com.bancoxyz.batch_legacy.repository;

import com.bancoxyz.batch_legacy.model.TransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaccionRepository extends JpaRepository<TransaccionEntity, Long> {
}