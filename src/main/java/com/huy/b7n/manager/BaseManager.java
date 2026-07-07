package com.huy.b7n.manager;

import com.huy.b7n.response.ResponseDto;
import org.springframework.http.ResponseEntity;

public abstract class BaseManager {
    protected ResponseEntity<?> success(Object response) {
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    protected ResponseEntity<?> error(Object response) {
        return ResponseEntity.ok(ResponseDto.error(response));
    }
}
