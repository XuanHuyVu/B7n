package com.tlu.tsms.service.dao;

import com.tlu.tsms.request.LoginRequestDto;
import com.tlu.tsms.entity.LoginLogEntity;
import com.tlu.tsms.entity.UserEntity;
import com.tlu.tsms.repository.LoginLogRepository;
import com.tlu.tsms.common.ELoginStatus;
import com.tlu.tsms.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class LoginLogDAO {

    private final LoginLogRepository loginLogRepository;

    public void save(UserEntity user, LoginRequestDto request, ELoginStatus status, String reason) {
        LoginLogEntity log = LoginLogEntity.builder()
                .userId(user.getId().intValue())
                .email(user.getEmail())
                .loginStatus(status)
                .userAgent(request.getUserAgent())
                .failureReason(reason)
                .loginAt(DateUtils.now())
                .build();
        loginLogRepository.save(log);
    }
}
