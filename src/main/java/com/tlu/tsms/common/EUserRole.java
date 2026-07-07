package com.tlu.tsms.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EUserRole {
    SYSTEM_ADMINISTRATOR("SYSTEM_ADMINISTRATOR"),
    ACADEMIC_AFFAIRS_OFFICER("ACADEMIC_AFFAIRS_OFFICER"),
    LECTURER("LECTURER"),
    STUDENT("STUDENT"),
    ;

    private final String role;

    public static EUserRole lookup(String role) {
        return Strings.isBlank(role) ? null : Arrays.stream(EUserRole.values())
                .filter(e -> role.equals(e.getRole()))
                .findFirst()
                .orElse(null);
    }
}
