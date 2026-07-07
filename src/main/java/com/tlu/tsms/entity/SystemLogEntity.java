package com.tlu.tsms.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlu.tsms.common.ESystemLogLevel;
import com.tlu.tsms.common.TableNameConstant;
import com.tlu.tsms.converter.ESystemLogLevelConverter;
import com.tlu.tsms.utils.DateUtils;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = TableNameConstant.SYSTEM_LOGS,
        indexes = {
                @Index(name = "idx_syslog_ts", columnList = "timestamp"),
                @Index(name = "idx_syslog_module_action", columnList = "module,action"),
                @Index(name = "idx_syslog_resource", columnList = "resourceType,resourceId"),
                @Index(name = "idx_syslog_trace", columnList = "traceId")
        }
)
public class SystemLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.NORMAL_TIME_PATTERN, timezone = DateUtils.DEFAULT_TIMEZONE_GMT7)
    private Date timestamp;
    @Convert(converter = ESystemLogLevelConverter.class)
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
    @Lob
    private String detailJson;
}
