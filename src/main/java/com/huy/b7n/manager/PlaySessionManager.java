package com.huy.b7n.manager;

import com.huy.b7n.dto.PlaySessionDto;
import com.huy.b7n.request.CreatePlaySessionRequest;
import com.huy.b7n.response.CreatePlaySessionResponse;
import com.huy.b7n.response.PlaySessionStateResponse;
import com.huy.b7n.response.SessionStatsResponse;
import com.huy.b7n.service.PlaySessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaySessionManager {

    private final PlaySessionService playSessionService;

    public CreatePlaySessionResponse createSession(CreatePlaySessionRequest request) {
        return playSessionService.createSession(request);
    }

    public void cancelSession(String sessionCode) {
        playSessionService.cancelSession(sessionCode);
    }

    public void completeSession(String sessionCode) {
        playSessionService.completeSession(sessionCode);
    }

    public SessionStatsResponse getSessionStats(String sessionCode) {
        return playSessionService.getSessionStats(sessionCode);
    }

    public PlaySessionStateResponse getSessionState(String sessionCode) {
        return playSessionService.getSessionState(sessionCode);
    }

    public List<PlaySessionDto> getSessions() {
        return playSessionService.getSessions();
    }
}