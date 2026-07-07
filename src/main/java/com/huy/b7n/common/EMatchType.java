package com.huy.b7n.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EMatchType {
    DOUBLES("Đánh đôi"),
    MIXED_DOUBLES("Đánh đôi nam nữ"),
    SINGLES("Đánh đơn");

    private final String matchType;

    public static EMatchType lookup(String matchType) {
        return Strings.isBlank(matchType) ? null : Arrays.stream(EMatchType.values())
                .filter(s -> matchType.equals(s.getMatchType()))
                .findFirst()
                .orElse(null);
    }
}
