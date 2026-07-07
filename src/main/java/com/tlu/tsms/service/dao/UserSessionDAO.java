package com.tlu.tsms.service.dao;

import com.tlu.tsms.common.Constant;
import com.tlu.tsms.request.LoginRequestDto;
import com.tlu.tsms.entity.UserEntity;
import com.tlu.tsms.entity.UserSessionEntity;
import com.tlu.tsms.repository.UserSessionRepository;
import com.tlu.tsms.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserSessionDAO {

    private final UserSessionRepository userSessionRepository;

    public void createSession(UserEntity user, String token, LoginRequestDto request) {
        UserSessionEntity session = UserSessionEntity.builder()
                .userId(user.getId().intValue())
                .sessionToken(token)
                .userAgent(request.getUserAgent())
                .createdDate(DateUtils.now())
                .expiresAt(Timestamp.from(Instant.now().plusSeconds(Constant.SESSION_TIMEOUT)))
                .lastActivityAt(new Timestamp(System.currentTimeMillis()))
                .isActive(true)
                .build();
        userSessionRepository.save(session);
    }

    public void cleanupOldSessions(Integer userId) {
        List<UserSessionEntity> sessions = userSessionRepository.findTop3ByUserIdOrderByCreatedDateDesc(userId);
        if (sessions.size() > Constant.MAX_DEVICE) {
            for (int i = Constant.MAX_DEVICE; i < sessions.size(); i++) {
                userSessionRepository.delete(sessions.get(i));
            }
        }
    }
}
