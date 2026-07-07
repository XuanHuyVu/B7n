package com.tlu.tsms.repository;

import com.tlu.tsms.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, Long> {
    List<UserSessionEntity> findTop3ByUserIdOrderByCreatedDateDesc(Integer userId);
}
