package com.huy.b7n.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EPlaySessionStatus {
    CREATED("Đã tạo"),
    IN_PROGRESS("Đang diễn ra"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy");

    private final String status;

    public static EPlaySessionStatus lookup(String status) {
        return Strings.isBlank(status) ? null : Arrays.stream(EPlaySessionStatus.values())
                .filter(s -> status.equals(s.getStatus()))
                .findFirst()
                .orElse(null);
    }
}