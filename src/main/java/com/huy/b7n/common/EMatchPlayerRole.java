package com.huy.b7n.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EMatchPlayerRole {
    MAIN("Người chơi chính"),
    SUBSTITUTE("Người thay thế"),
    REPLACED("Người bị thay ra");

    private final String role;

    public static EMatchPlayerRole lookup(String role) {
        return Strings.isBlank(role) ? null : Arrays.stream(EMatchPlayerRole.values())
                .filter(s -> role.equals(s.getRole()))
                .findFirst()
                .orElse(null);
    }
}
