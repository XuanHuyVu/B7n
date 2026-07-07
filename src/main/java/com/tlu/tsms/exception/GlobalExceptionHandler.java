package com.tlu.tsms.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handle(BaseException ex) {
        EStatusCode sc = ex.getStatusCode();
        return ResponseEntity.status(sc.getStatus()).body(Map.of(
                "status", sc.getStatus(),
                "code", sc.getCode(),
                "message", sc.getMessage(),
                "data", ex.getData()
        ));
    }
}