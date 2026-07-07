package com.tlu.tsms.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ELoginStatus {
    LOGIN_SUCCESS("LOGIN_SUCCESS"),
    LOGIN_FAILURE("LOGIN_FAILURE"),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED"),
    ACCOUNT_INACTIVE("ACCOUNT_INACTIVE"),
    ACCOUNT_SUSPENDED("ACCOUNT_SUSPENDED"),
    ACCOUNT_BLOCKED("ACCOUNT_BLOCKED"),
    PASSWORD_EXPIRED("PASSWORD_EXPIRED"),
    OTP_REQUIRED("OTP_REQUIRED"),
    OTP_FAILED("OTP_FAILED"),
    ;

    private final String status;

    public static ELoginStatus lookup(String status) {
        return Strings.isBlank(status) ? null : Arrays.stream(ELoginStatus.values())
                .filter(e -> status.equals(e.getStatus()))
                .findFirst()
                .orElse(null);
    }
}