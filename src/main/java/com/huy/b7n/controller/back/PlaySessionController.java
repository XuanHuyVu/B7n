package com.huy.b7n.controller.back;

import com.huy.b7n.controller.BaseController;
import com.huy.b7n.dto.PlaySessionDto;
import com.huy.b7n.manager.PlaySessionManager;
import com.huy.b7n.request.CreatePlaySessionRequest;
import com.huy.b7n.response.CreatePlaySessionResponse;
import com.huy.b7n.response.PlaySessionStateResponse;
import com.huy.b7n.response.ResponseDto;
import com.huy.b7n.response.SessionStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/play-sessions")
@RequiredArgsConstructor
public class PlaySessionController extends BaseController {

    private final PlaySessionManager playSessionManager;

    @PostMapping
    public ResponseEntity<ResponseDto<CreatePlaySessionResponse>> createSession(@RequestBody CreatePlaySessionRequest request) {
        return success(playSessionManager.createSession(request));
    }

    @GetMapping
    public ResponseEntity<ResponseDto<List<PlaySessionDto>>> getSessions() {
        return success(playSessionManager.getSessions());
    }

    @GetMapping("/{sessionCode}")
    public ResponseEntity<ResponseDto<PlaySessionStateResponse>> getSessionState(@PathVariable String sessionCode) {
        return success(playSessionManager.getSessionState(sessionCode));
    }

    @PostMapping("/{sessionCode}/complete")
    public ResponseEntity<ResponseDto<?>> completeSession(@PathVariable String sessionCode) {
        playSessionManager.completeSession(sessionCode);
        return success();
    }

    @DeleteMapping("/{sessionCode}")
    public ResponseEntity<ResponseDto<?>> cancelSession(@PathVariable String sessionCode) {
        playSessionManager.cancelSession(sessionCode);
        return success();
    }

    @GetMapping("/{sessionCode}/stats")
    public ResponseEntity<ResponseDto<SessionStatsResponse>> getSessionStats(@PathVariable String sessionCode) {
        return success(playSessionManager.getSessionStats(sessionCode));
    }
}