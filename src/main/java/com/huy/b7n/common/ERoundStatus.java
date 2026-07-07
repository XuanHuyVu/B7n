package com.huy.b7n.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ERoundStatus {
    SCHEDULED("Đã lên lịch"),
    IN_PROGRESS("Đang diễn ra"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy");

    private final String status;

    public static ERoundStatus lookup(String status) {
        return Strings.isBlank(status) ? null : Arrays.stream(ERoundStatus.values())
                .filter(s -> status.equals(s.getStatus()))
                .findFirst()
                .orElse(null);
    }
}