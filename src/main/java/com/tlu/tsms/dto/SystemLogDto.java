package com.tlu.tsms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlu.tsms.common.ESystemLogLevel;
import com.tlu.tsms.utils.DateUtils;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogDto {
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.NORMAL_TIME_PATTERN, timezone = DateUtils.DEFAULT_TIMEZONE_GMT7)
    private Date timestamp;
    private ESystemLogLevel level;
    private String module;
    private String action;
    private String actorName;
    private String ip;
    private String userAgent;
    private String traceId;
    private String resourceType;
    private Long resourceId;
    private String message;
    private String detailJson;
}
