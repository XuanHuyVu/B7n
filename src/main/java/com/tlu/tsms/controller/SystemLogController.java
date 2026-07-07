package com.tlu.tsms.controller;

import com.tlu.tsms.common.ESystemLogLevel;
import com.tlu.tsms.dto.SystemLogDto;
import com.tlu.tsms.manager.SystemLogManager;
import com.tlu.tsms.response.PageResponse;
import com.tlu.tsms.response.ResponseDto;
import com.tlu.tsms.utils.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import static com.tlu.tsms.common.Constant.DEFAULT_PAGE;
import static com.tlu.tsms.common.Constant.DEFAULT_PAGE_SIZE;

@RestController
@RequestMapping("/v1/admin/system-logs")
@RequiredArgsConstructor
public class SystemLogController extends BaseController{

    private final SystemLogManager manager;

    @GetMapping
    public ResponseEntity<ResponseDto<PageResponse<SystemLogDto>>> page(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ESystemLogLevel level,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actorName,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) Date from,
            @RequestParam(required = false) Date to,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort
    ) {
        Pageable pageable = PageableUtils.pageable(page, size, sort);
        PageResponse<SystemLogDto> data = PageResponse.toResponse(
                manager.search(q, level, module, action, actorName, traceId, from, to, pageable)
        );
        return success(data);
    }


    @GetMapping("/{id}")
    public SystemLogDto getById(@PathVariable Long id) {
        return manager.getById(id);
    }
}
