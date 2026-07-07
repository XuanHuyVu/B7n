package com.tlu.tsms.service.dao;

import com.tlu.tsms.common.ESystemLogLevel;
import com.tlu.tsms.entity.SystemLogEntity;
import com.tlu.tsms.repository.SystemLogRepository;
import com.tlu.tsms.logging.core.SystemLogSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SystemLogDAO {

    private final SystemLogRepository repository;

    public SystemLogEntity save(SystemLogEntity e) {
        return repository.save(e);
    }

    public Page<SystemLogEntity> search(String q, ESystemLogLevel level, String module, String action,
                                        String actorName, String traceId, Date from, Date to, Pageable pageable) {
        return repository.findAll(SystemLogSpecs.filter(q, level, module, action, actorName, traceId, from, to), pageable);
    }

    public Optional<SystemLogEntity> findById(Long id) {
        return repository.findById(id);
    }
}
