package com.tlu.tsms.service;

import com.tlu.tsms.common.ESystemLogLevel;
import com.tlu.tsms.dto.SystemLogDto;
import com.tlu.tsms.entity.SystemLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public interface SystemLogService {

    @Transactional
    void write(SystemLogEntity e);

    Page<SystemLogDto> search(
            String q,
            ESystemLogLevel level,
            String module,
            String action,
            String actorName,
            String traceId,
            Date from,
            Date to,
            Pageable pageable
    );

    SystemLogDto getById(Long id);
}
