package com.huy.b7n.service;

import com.huy.b7n.dto.PlaySessionDto;
import com.huy.b7n.request.CreatePlaySessionRequest;
import com.huy.b7n.response.CreatePlaySessionResponse;
import com.huy.b7n.response.PlaySessionStateResponse;
import com.huy.b7n.response.SessionStatsResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PlaySessionService {

    @Transactional
    CreatePlaySessionResponse createSession(CreatePlaySessionRequest request);

    List<PlaySessionDto> getSessions();

    @Transactional
    void cancelSession(String sessionCode);

    @Transactional
    void completeSession(String sessionCode);

    SessionStatsResponse getSessionStats(String sessionCode);

    PlaySessionStateResponse getSessionState(String sessionCode);
}