package com.tlu.tsms.service.impl;

import com.tlu.tsms.common.ESystemLogLevel;
import com.tlu.tsms.dto.SystemLogDto;
import com.tlu.tsms.entity.SystemLogEntity;
import com.tlu.tsms.service.SystemLogService;
import com.tlu.tsms.service.dao.SystemLogDAO;
import com.tlu.tsms.utils.ErrorUtils;
import com.tlu.tsms.exception.EStatusCode;
import com.tlu.tsms.utils.MapperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogDAO dao;

    @Override
    public void write(SystemLogEntity e) {
        dao.save(e);
    }

    @Override
    public Page<SystemLogDto> search(String q, ESystemLogLevel level, String module, String action, String actorName, String traceId, Date from, Date to, Pageable pageable) {
        Page<SystemLogEntity> page  = dao.search(q, level, module, action, actorName, traceId, from, to, pageable);
        return page.map(entity -> Objects.requireNonNull(MapperUtils.convertValue(entity, SystemLogDto.class)));
    }

    @Override
    @Cacheable(cacheNames = "systemLogs", key = "#id")
    public SystemLogDto getById(Long id) {
        SystemLogEntity entity = dao.findById(id)
                .orElseThrow(() -> ErrorUtils.exception(EStatusCode.NOT_FOUND, Map.of("log", "not found")));
        return MapperUtils.convertValue(entity, SystemLogDto.class);
    }
}
