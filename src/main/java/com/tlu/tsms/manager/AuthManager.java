package com.tlu.tsms.manager;

import com.tlu.tsms.logging.ManagerIf;
import com.tlu.tsms.request.LoginRequestDto;
import com.tlu.tsms.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ManagerIf
public class AuthManager extends BaseManager {

    private final AuthService authService;

    public ResponseEntity<?> login(LoginRequestDto request, String userAgent) {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .userAgent(userAgent)
                .build();
        return success(authService.login(loginRequest));
    }
}
