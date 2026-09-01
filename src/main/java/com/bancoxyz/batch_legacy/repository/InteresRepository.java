package com.bancoxyz.batch_legacy.repository;

import com.bancoxyz.batch_legacy.model.InteresEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteresRepository extends JpaRepository<InteresEntity, Long> {
}