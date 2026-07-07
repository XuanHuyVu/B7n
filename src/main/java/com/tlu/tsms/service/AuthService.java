package com.tlu.tsms.service;

import com.tlu.tsms.request.LoginRequestDto;
import com.tlu.tsms.dto.LoginResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto request);
}
