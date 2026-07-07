package com.tlu.tsms.service.impl;

import com.tlu.tsms.common.Constant;
import com.tlu.tsms.common.EAccountStatus;
import com.tlu.tsms.common.ELoginStatus;
import com.tlu.tsms.exception.EStatusCode;
import com.tlu.tsms.request.LoginRequestDto;
import com.tlu.tsms.dto.LoginResponseDto;
import com.tlu.tsms.entity.UserEntity;
import com.tlu.tsms.service.AuthService;
import com.tlu.tsms.service.BaseService;
import com.tlu.tsms.service.dao.LoginLogDAO;
import com.tlu.tsms.service.dao.UserDAO;
import com.tlu.tsms.service.dao.UserSessionDAO;
import com.tlu.tsms.utils.DateUtils;
import com.tlu.tsms.utils.ErrorUtils;
import com.tlu.tsms.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends BaseService implements AuthService {

    private final UserDAO userDAO;
    private final LoginLogDAO loginLogDAO;
    private final UserSessionDAO userSessionDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        UserEntity user = Optional.ofNullable(request)
                .map(LoginRequestDto::getEmail)
                .map(String::trim)
                .filter(email -> !email.isBlank())
                .flatMap(userDAO::findByEmail)
                .orElseThrow(() -> ErrorUtils.exception(EStatusCode.USER_NOT_FOUND, "user"));
        checkUnlock(user);
        validate(user, request);
        loginSuccess(user, request);
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        userSessionDAO.createSession(user, token, request);
        userSessionDAO.cleanupOldSessions(user.getId().intValue());
        log.info("Login success: {}", user.getEmail());
        return LoginResponseDto.builder()
                .accessToken(token)
                .build();
    }

    private void checkUnlock(UserEntity user) {
        Optional.ofNullable(user)
                .filter(u -> Objects.nonNull(u.getLockedUntil()))
                .filter(u -> u.getLockedUntil().before(DateUtils.now()))
                .map(u -> {
                    u.setIsLocked(false);
                    u.setLockedUntil(null);
                    u.setLockedReason(null);
                    u.setFailedLoginAttempts(Constant.DEFAULT_ATTEMPT);
                    u.setAccountStatus(EAccountStatus.ACTIVE);
                    return u;
                }).ifPresent(userDAO::save);
    }

    private void validate(UserEntity user, LoginRequestDto request) {
        Optional.ofNullable(user).ifPresent(u -> {
            Optional.ofNullable(u.getIsLocked())
                    .filter(Boolean.TRUE::equals)
                    .ifPresent(locked -> {
                        loginLogDAO.save(u, LoginRequestDto.EMPTY, ELoginStatus.ACCOUNT_LOCKED, EStatusCode.LOGIN_ACCOUNT_LOCKED.getMessage());
                        throw ErrorUtils.exception(EStatusCode.LOGIN_ACCOUNT_LOCKED, "account");
                    });
            Optional.ofNullable(u.getAccountStatus())
                    .filter(status -> !EAccountStatus.ACTIVE.equals(status))
                    .ifPresent(status -> {
                        loginLogDAO.save(u, LoginRequestDto.EMPTY, ELoginStatus.ACCOUNT_INACTIVE, EStatusCode.LOGIN_ACCOUNT_INACTIVE.getMessage());
                        throw ErrorUtils.exception(EStatusCode.LOGIN_ACCOUNT_INACTIVE, "account");
                    });
            Optional.ofNullable(request)
                    .filter(r -> !passwordEncoder.matches(r.getPassword(), u.getPasswordHash()))
                    .ifPresent(r -> {
                        failedLogin(u, r);
                        throw ErrorUtils.exception(EStatusCode.LOGIN_WRONG_PASSWORD, "password");
                    });
        });
    }


    private void loginSuccess(UserEntity user, LoginRequestDto request) {
        Optional.ofNullable(user)
                .map(u -> {
                    u.setFailedLoginAttempts(Constant.DEFAULT_ATTEMPT);
                    u.setLastLoginAt(DateUtils.now());
                    u.setLastLoginDevice(request.getUserAgent());
                    u.setAccountStatus(EAccountStatus.ACTIVE);
                    return u;
                })
                .ifPresent(u -> {
                    userDAO.save(u);
                    loginLogDAO.save(u, request, ELoginStatus.LOGIN_SUCCESS, null);
                });
    }

    private void failedLogin(UserEntity user, LoginRequestDto request) {
        Optional.ofNullable(user)
                .map(u -> {
                    int attempts = Objects.requireNonNullElse(u.getFailedLoginAttempts(), Constant.DEFAULT_ATTEMPT) + 1;
                    u.setFailedLoginAttempts(attempts);
                    u.setLastFailedLoginAt(DateUtils.now());
                    Optional.of(attempts)
                            .filter(a -> a >= Constant.MAX_LOGIN)
                            .ifPresent(a -> {
                                u.setIsLocked(true);
                                u.setLockedUntil(Date.from(Instant.now().plusSeconds(Constant.LOCK_TIME)));
                                u.setAccountStatus(EAccountStatus.BLOCKED);
                                u.setLockedReason(EStatusCode.LOGIN_WRONG_PASSWORD.getMessage());
                            });
                    return u;
                })
                .ifPresent(u -> {
                    userDAO.save(u);
                    loginLogDAO.save(u, request, ELoginStatus.LOGIN_FAILURE, EStatusCode.LOGIN_WRONG_PASSWORD.getMessage());
                });
    }
}