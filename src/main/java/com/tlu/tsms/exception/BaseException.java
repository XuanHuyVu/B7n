package com.tlu.tsms.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final EStatusCode statusCode;
    private final Object data;

    public BaseException(EStatusCode statusCode, String message, Object data) {
        super(message);
        this.statusCode = statusCode;
        this.data = data;
    }
}
