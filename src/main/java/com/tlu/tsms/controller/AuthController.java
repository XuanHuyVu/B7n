package com.tlu.tsms.controller;

import com.tlu.tsms.common.Constant;
import com.tlu.tsms.logging.AuditLog;
import com.tlu.tsms.logging.ManagerIf;
import com.tlu.tsms.request.LoginRequestDto;
import com.tlu.tsms.manager.AuthManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthManager authManager;

    @PostMapping("/login")
    @AuditLog(module="AUTH", action="LOGIN", resourceType="USER", resourceId="#result.userId")
    @ManagerIf()
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDto request,
            @RequestHeader(value = Constant.USER_AGENT) String userAgent
    ) {
        return authManager.login(request, userAgent);
    }
}