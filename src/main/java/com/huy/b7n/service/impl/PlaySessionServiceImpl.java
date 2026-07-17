package com.huy.b7n.service.impl;

import com.huy.b7n.common.EMatchType;
import com.huy.b7n.common.EPlaySessionStatus;
import com.huy.b7n.common.ERoundStatus;
import com.huy.b7n.common.ESessionPlayerStatus;
import com.huy.b7n.dto.*;
import com.huy.b7n.entity.*;
import com.huy.b7n.request.CreatePlaySessionRequest;
import com.huy.b7n.response.CreatePlaySessionResponse;
import com.huy.b7n.response.GenerateRoundResponse;
import com.huy.b7n.response.PlaySessionStateResponse;
import com.huy.b7n.response.SessionStatsResponse;
import com.huy.b7n.service.BaseService;
import com.huy.b7n.service.PlaySessionService;
import com.huy.b7n.service.dao.PlaySessionDAO;
import com.huy.b7n.service.dao.PlayerDAO;
import com.huy.b7n.service.dao.ScheduleDAO;
import com.huy.b7n.utils.DateUtils;
import com.huy.b7n.utils.MapperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PlaySessionServiceImpl extends BaseService implements PlaySessionService {

    private final PlayerDAO playerDao;
    private final PlaySessionDAO playSessionDao;
    private final ScheduleDAO scheduleDao;

    private static final DateTimeFormatter SESSION_CODE_DATE_FORMAT = DateTimeFormatter.ofPattern(DateUtils.DDMMYYYY);
    private static final List<ERoundStatus> OPEN_ROUND_STATUSES = List.of(ERoundStatus.SCHEDULED, ERoundStatus.IN_PROGRESS);

    @Override
    public CreatePlaySessionResponse createSession(CreatePlaySessionRequest request) {
        String sessionCode = generateSessionCode();
        validateCreateSessionRequest(request, sessionCode, request.getPlayerCodes());
        PlaySessionEntity session = Objects.requireNonNull(MapperUtils.convertValue(request, PlaySessionEntity.class));
        session.setSessionCode(sessionCode);
        session.setStatus(EPlaySessionStatus.CREATED);
        session.setMatchType(EMatchType.DOUBLES);
        session.setCreatedAt(DateUtils.now());
        session.setStartedAt(DateUtils.now());
        session = playSessionDao.saveSession(session);
        List<PlayerEntity> players = request.getPlayerCodes().stream().map(playerDao::getRequired).toList();
        List<SessionPlayerEntity> sessionPlayers = createSessionPlayers(session, players);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("session", MapperUtils.convertValue(session, PlaySessionDto.class));
        responseMap.put("players", mapList(sessionPlayers, SessionPlayerDto.class));
        return MapperUtils.convertValue(responseMap, CreatePlaySessionResponse.class);
    }

    @Override
    public List<PlaySessionDto> getSessions() {
        return playSessionDao.findSessions().stream()
                .map(session -> MapperUtils.convertValue(session, PlaySessionDto.class))
                .toList();
    }

    @Override
    public void cancelSession(String sessionCode) {
        playSessionDao.getSessionRequired(sessionCode);
        scheduleDao.deleteMatchPlayersBySession(sessionCode);
        scheduleDao.deleteMatchesBySession(sessionCode);
        scheduleDao.deleteRoundsBySession(sessionCode);
        playSessionDao.deleteSessionPlayers(sessionCode);
        playSessionDao.deleteSession(sessionCode);
    }

    @Override
    public void completeSession(String sessionCode) {
        PlaySessionEntity session = playSessionDao.getSessionRequired(sessionCode);
        RoundEntity openRound = scheduleDao.findLatestRoundByStatuses(sessionCode, OPEN_ROUND_STATUSES);
        Assert.isNull(openRound, "Cần hoàn thành vòng đang xếp trước khi kết thúc ca chơi.");
        session.setStatus(EPlaySessionStatus.COMPLETED);
        session.setEndedAt(DateUtils.now());
        playSessionDao.saveSession(session);
    }

    @Override
    public SessionStatsResponse getSessionStats(String sessionCode) {
        playSessionDao.getSessionRequired(sessionCode);
        return new SessionStatsResponse(sessionCode, getSessionPlayerStats(sessionCode));
    }

    @Override
    public PlaySessionStateResponse getSessionState(String sessionCode) {
        PlaySessionEntity session = playSessionDao.getSessionRequired(sessionCode);
        return new PlaySessionStateResponse(
                MapperUtils.convertValue(session, PlaySessionDto.class),
                getCurrentRound(sessionCode),
                getSessionPlayerStats(sessionCode)
        );
    }

    private GenerateRoundResponse getCurrentRound(String sessionCode) {
        RoundEntity round = scheduleDao.findLatestRoundByStatuses(sessionCode, OPEN_ROUND_STATUSES);
        if (Objects.isNull(round)) {
            return null;
        }

        List<MatchEntity> matches = scheduleDao.findMatchesByRound(sessionCode, round.getRoundNumber());
        return new GenerateRoundResponse(toRoundDto(round), toMatchDtos(matches), List.of());
    }

    private RoundDto toRoundDto(RoundEntity round) {
        RoundDto dto = new RoundDto();
        dto.setSessionCode(round.getSession().getSessionCode());
        dto.setRoundNumber(round.getRoundNumber());
        dto.setStatus(round.getStatus());
        dto.setStartedAt(round.getStartedAt());
        dto.setEndedAt(round.getEndedAt());
        dto.setCreatedAt(round.getCreatedAt());
        return dto;
    }

    private List<MatchDto> toMatchDtos(List<MatchEntity> matches) {
        return matches.stream().map(this::toMatchDto).toList();
    }

    private MatchDto toMatchDto(MatchEntity match) {
        String sessionCode = match.getRound().getSession().getSessionCode();
        Integer roundNumber = match.getRound().getRoundNumber();
        Integer courtNumber = match.getCourtNumber();
        List<MatchPlayerEntity> matchPlayers = scheduleDao.findMatchPlayers(sessionCode, roundNumber, courtNumber);
        return buildMatchDto(match, sessionCode, roundNumber, courtNumber, matchPlayers);
    }

    private List<SessionPlayerStatDto> getSessionPlayerStats(String sessionCode) {
        return playSessionDao.findSessionPlayers(sessionCode).stream()
                .sorted(Comparator.comparing(sp -> sp.getPlayer().getName(), Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toSessionPlayerStatDto)
                .toList();
    }

    private SessionPlayerStatDto toSessionPlayerStatDto(SessionPlayerEntity sessionPlayer) {
        SessionPlayerStatDto dto = new SessionPlayerStatDto();
        dto.setPlayer(MapperUtils.convertValue(sessionPlayer.getPlayer(), PlayerDto.class));
        dto.setMatchCount(valueOrZero(sessionPlayer.getMatchCount()));
        dto.setRestCount(valueOrZero(sessionPlayer.getRestCount()));
        dto.setConsecutiveMatchCount(valueOrZero(sessionPlayer.getConsecutiveMatchCount()));
        return dto;
    }

    private void validateCreateSessionRequest(CreatePlaySessionRequest request,
                                              String sessionCode, List<String> playerCodes) {
        Assert.isTrue(!playSessionDao.existsBySessionCode(sessionCode),
                "Ca chơi hôm nay đã được tạo: " + sessionCode);
        Assert.isTrue(Objects.nonNull(request.getCourtCount()) && request.getCourtCount() > 0,
                "Số sân phải lớn hơn 0");
        Assert.notEmpty(playerCodes, "Không có người chơi trong danh sách");
        playerCodes.forEach(code -> Assert.hasText(code, "Mã người chơi không được rỗng"));
        long distinctCount = playerCodes.stream().distinct().count();
        Assert.isTrue(distinctCount == playerCodes.size(), "Trùng mã người chơi.");
    }

    private List<SessionPlayerEntity> createSessionPlayers(PlaySessionEntity session, List<PlayerEntity> players) {
        Function<PlayerEntity, SessionPlayerEntity> buildSessionPlayer = player -> {
            SessionPlayerEntity sessionPlayer = new SessionPlayerEntity();
            sessionPlayer.setSession(session);
            sessionPlayer.setPlayer(player);
            sessionPlayer.setCurrentStatus(ESessionPlayerStatus.AVAILABLE);
            sessionPlayer.setMatchCount(0);
            sessionPlayer.setRestCount(0);
            sessionPlayer.setConsecutiveMatchCount(0);
            sessionPlayer.setJoinedAt(new Date());
            return sessionPlayer;
        };
        List<SessionPlayerEntity> sessionPlayers = players.stream()
                .map(buildSessionPlayer)
                .toList();
        return playSessionDao.saveSessionPlayers(sessionPlayers);
    }

    private String generateSessionCode() {
        String prefix = "BMT" + LocalDate.now().format(SESSION_CODE_DATE_FORMAT);
        long sequence = playSessionDao.countSessionCodeStartingWith(prefix) + 1;
        return prefix + "-" + sequence;
    }
}