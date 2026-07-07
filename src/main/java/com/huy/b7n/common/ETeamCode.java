package com.huy.b7n.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ETeamCode {
    A("A"),
    B("B");

    private final String teamCode;

    public static ETeamCode lookup(String teamCode) {
        return Strings.isBlank(teamCode) ? null : Arrays.stream(ETeamCode.values())
                .filter(s -> teamCode.equals(s.getTeamCode()))
                .findFirst()
                .orElse(null);
    }
}