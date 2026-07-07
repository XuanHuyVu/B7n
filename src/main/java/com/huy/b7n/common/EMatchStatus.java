package com.huy.b7n.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EMatchStatus {
    SCHEDULED("Đã lên lịch"),
    IN_PROGRESS("Đang diễn ra"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy"),
    ENDED_EARLY("Kết thúc sớm");

    private final String status;

    public static EMatchStatus lookup(String status) {
        return Strings.isBlank(status) ? null : Arrays.stream(EMatchStatus.values())
                .filter(s -> status.equals(s.getStatus()))
                .findFirst()
                .orElse(null);
    }
}