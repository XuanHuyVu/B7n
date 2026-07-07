package com.tlu.tsms.controller;

import com.tlu.tsms.exception.EStatusCode;
import com.tlu.tsms.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {
    protected ResponseEntity<ResponseDto<?>> success() {
        return ResponseEntity.ok(ResponseDto.builder()
                .status(HttpStatus.OK.value())
                .code(String.valueOf(HttpStatus.OK.value()))
                .message(EStatusCode.SUCCESS.getMessage())
                .build());
    }

    protected <T> ResponseEntity<ResponseDto<T>> success(T data) {
        return ResponseEntity.ok(ResponseDto.<T>builder()
                .status(HttpStatus.OK.value())
                .code(String.valueOf(HttpStatus.OK.value()))
                .message(EStatusCode.SUCCESS.getMessage())
                .data(data)
                .build());
    }
}
