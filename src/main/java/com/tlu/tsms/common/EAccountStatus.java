package com.tlu.tsms.common;

import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EAccountStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    SUSPENDED("SUSPENDED"),
    BLOCKED("BLOCKED"),
    DELETED("DELETED"),
    ;

    private final String status;

    public static EAccountStatus lookup(String status) {
        return StringUtils.isBlank(status) ? null : Arrays.stream(EAccountStatus.values())
                .filter(e -> status.equals(e.getStatus()))
                .findFirst()
                .orElse(null);
    }
}

