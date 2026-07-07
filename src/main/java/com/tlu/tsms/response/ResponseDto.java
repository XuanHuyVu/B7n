package com.tlu.tsms.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tlu.tsms.exception.EStatusCode;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {
    private int status;
    private String code;
    private String message;
    private String messageCode;
    private T data;

    public static ResponseDto<?> success(Object response) {
        return ResponseDto.builder()
                .code(EStatusCode.SUCCESS.getCode())
                .status(EStatusCode.SUCCESS.getStatus())
                .message(EStatusCode.SUCCESS.getMessage())
                .data(response)
                .build();
    }

    public static ResponseDto<?> error(Object response) {
        return ResponseDto.builder()
                .code(EStatusCode.ERROR.getCode())
                .status(EStatusCode.ERROR.getStatus())
                .message(EStatusCode.ERROR.getMessage())
                .data(response)
                .build();
    }

    public static ResponseDto<?> error(EStatusCode statusCode, Object data) {
        return ResponseDto.builder()
                .status(statusCode.getStatus())
                .code(statusCode.getCode())
                .message(statusCode.getMessage())
                .data(data)
                .build();
    }
}
