package com.tlu.tsms.manager;

import com.tlu.tsms.common.ESystemLogLevel;
import com.tlu.tsms.logging.ManagerIf;
import com.tlu.tsms.dto.SystemLogDto;
import com.tlu.tsms.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
@ManagerIf
public class SystemLogManager {

    private final SystemLogService service;

    public Page<SystemLogDto> search(
            String q,
            ESystemLogLevel level,
            String module,
            String action,
            String actorName,
            String traceId,
            Date from,
            Date to,
            Pageable pageable
    ) {
        return service.search(q, level, module, action, actorName, traceId, from, to, pageable);
    }

    public SystemLogDto getById(Long id) {
        return service.getById(id);
    }
}
