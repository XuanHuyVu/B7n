package com.tlu.tsms.utils;

import com.tlu.tsms.exception.BaseException;
import com.tlu.tsms.exception.EStatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ErrorUtils {
    public static BaseException exception(EStatusCode sc, Object data) {
        return new BaseException(sc, sc.getMessage(), data);
    }
}
