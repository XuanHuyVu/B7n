package com.tlu.tsms.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ESystemLogLevel {
    INFO("INFO"),
    WARN("WARNING"),
    ERROR("ERROR"),
    DEBUG("DEBUG"),
    TRACE("TRACE")
    ;

    private final String level;

    public static ESystemLogLevel lookup(String level) {
        return Strings.isBlank(level) ? null : Arrays.stream(ESystemLogLevel.values())
                .filter(e -> level.equals(e.getLevel()))
                .findFirst()
                .orElse(null);
    }
}
