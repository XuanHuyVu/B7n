package com.huy.b7n.utils;

import com.huy.b7n.exception.BaseException;
import com.huy.b7n.exception.EStatusCode;
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
