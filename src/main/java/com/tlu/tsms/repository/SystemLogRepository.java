package com.tlu.tsms.repository;

import com.tlu.tsms.entity.SystemLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SystemLogRepository extends JpaRepository<SystemLogEntity, Long>, JpaSpecificationExecutor<SystemLogEntity> {
}
