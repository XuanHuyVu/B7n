package com.tlu.tsms.manager;

import com.tlu.tsms.response.ResponseDto;
import org.springframework.http.ResponseEntity;

public abstract class BaseManager {
    protected ResponseEntity<?> success(Object response) {
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    protected ResponseEntity<?> error(Object response) {
        return ResponseEntity.ok(ResponseDto.error(response));
    }
}
