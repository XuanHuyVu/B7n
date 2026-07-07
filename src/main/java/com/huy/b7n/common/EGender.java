package com.huy.b7n.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EGender {
    MALE("Nam"),
    FEMALE("Nữ");

    private final String gender;

    public static EGender lookup(String gender) {
        return Strings.isBlank(gender) ? null : Arrays.stream(EGender.values())
                .filter(s -> gender.equals(s.getGender()))
                .findFirst()
                .orElse(null);
    }
}
