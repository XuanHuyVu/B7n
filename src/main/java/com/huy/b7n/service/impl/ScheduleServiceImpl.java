package com.huy.b7n.service.impl;

import com.huy.b7n.common.*;
import com.huy.b7n.dto.*;
import com.huy.b7n.entity.*;
import com.huy.b7n.request.CompleteRoundRequest;
import com.huy.b7n.request.GenerateNextRoundRequest;
import com.huy.b7n.response.GenerateRoundResponse;
import com.huy.b7n.response.HistoryResponse;
import com.huy.b7n.response.RankingResponse;
import com.huy.b7n.service.BaseService;
import com.huy.b7n.service.ScheduleService;
import com.huy.b7n.service.dao.PlaySessionDAO;
import com.huy.b7n.service.dao.ScheduleDAO;
import com.huy.b7n.utils.MapperUtils;
import com.huy.b7n.utils.SessionPlayerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl extends BaseService implements ScheduleService {

    private final PlaySessionDAO playSessionDao;
    private final ScheduleDAO scheduleDao;
    private final SessionPlayerMapper  sessionPlayerMapper;

    @Override
    public GenerateRoundResponse generateNextRound(GenerateNextRoundRequest request) {
        PlaySessionEntity session = playSessionDao.getSessionRequired(request.getSessionCode());
        if (!EPlaySessionStatus.IN_PROGRESS.equals(session.getStatus())) {
            session.setStatus(EPlaySessionStatus.IN_PROGRESS);
            playSessionDao.saveSession(session);
        }
        validateNoUnfinishedRound(request.getSessionCode());
        List<SessionPlayerEntity> queriedPlayers = playSessionDao.findSessionPlayersByStatuses(
                request.getSessionCode(), List.of(ESessionPlayerStatus.AVAILABLE, ESessionPlayerStatus.RESTING));
        List<SessionPlayerEntity> activePlayers = distinctSessionPlayersByPlayerId(queriedPlayers);
        int playersPerMatch = Constant.ScheduleAlgorithmConfig.PLAYERS_PER_DOUBLES_MATCH;
        int matchCount = Math.min(session.getCourtCount(), activePlayers.size() / playersPerMatch);
        if (matchCount == 0)
            throw new IllegalArgumentException("Không đủ " + playersPerMatch + " người chơi khác nhau để tạo trận. "
                    + "Hiện chỉ có " + activePlayers.size() + " người");
        int playerNeeded = matchCount * playersPerMatch;
        Integer roundNumber = scheduleDao.getNextRoundNumber(request.getSessionCode());
        HistoryIndex historyIndex = buildHistoryIndex(request.getSessionCode());
        List<SessionPlayerEntity> selectedPlayers = selectPlayers(activePlayers, playerNeeded, roundNumber);
        Set<Long> selectedPlayerIds = selectedPlayers.stream()
                .map(SessionPlayerEntity::getPlayer)
                .map(PlayerEntity::getId)
                .collect(Collectors.toSet());
        List<SessionPlayerEntity> restingPlayers = activePlayers.stream()
                .filter(player -> !selectedPlayerIds.contains(player.getPlayer().getId()))
                .toList();
        RoundEntity round = new RoundEntity();
        round.setSession(session);
        round.setRoundNumber(roundNumber);
        round.setStatus(ERoundStatus.SCHEDULED);
        round.setCreatedAt(new Date());
        round = scheduleDao.saveRound(round);
        List<MatchEntity> matches = createMatches(round, selectedPlayers, matchCount, historyIndex);
        updateSessionPlayersAfterSchedule(selectedPlayers, restingPlayers, roundNumber);
        return new GenerateRoundResponse(toRoundDto(round), toMatchDtos(matches), sessionPlayerMapper.toDtos(restingPlayers));
    }

    @Override
    public GenerateRoundResponse completeRound(CompleteRoundRequest request) {
        RoundEntity round = scheduleDao.getRoundRequired(request.getSessionCode(), request.getRoundNumber());
        List<MatchEntity> matches = scheduleDao.findMatchesByRound(request.getSessionCode(), request.getRoundNumber());
        Map<Integer, ETeamCode> winnersByCourt = Optional.ofNullable(request.getResults()).orElse(List.of()).stream()
                .filter(result -> Objects.nonNull(result.getCourtNumber()) && Objects.nonNull(result.getWinner()))
                .collect(Collectors.toMap(
                        CompleteRoundRequest.MatchResultRequest::getCourtNumber,
                        CompleteRoundRequest.MatchResultRequest::getWinner,
                        (first, second) -> second
                ));
        Date now = new Date();
        matches.forEach(match -> completeMatch(request.getSessionCode(), request.getRoundNumber(), match,
                winnersByCourt.get(match.getCourtNumber()), now));
        round.setStatus(ERoundStatus.COMPLETED);
        round.setEndedAt(now);
        round = scheduleDao.saveRound(round);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("round", toRoundDto(round));
        responseMap.put("matches", toMatchDtos(matches));
        responseMap.put("restingPlayers", List.of());
        return MapperUtils.convertValue(responseMap, GenerateRoundResponse.class);
    }

    @Override
    public HistoryResponse getHistory(String sessionCode) {
        List<MatchHistoryDto> history = scheduleDao.findCompletedMatches(sessionCode).stream()
                .map(this::toHistoryDto)
                .toList();
        return new HistoryResponse(history);
    }

    @Override
    public RankingResponse getRanking(String sessionCode) {
        Map<String, RankingAccumulator> rows = new LinkedHashMap<>();
        for (MatchPlayerEntity matchPlayer : scheduleDao.findCompletedMatchPlayers(sessionCode)) {
            String playerCode = matchPlayer.getPlayer().getPlayerCode();
            RankingAccumulator row = rows.computeIfAbsent(playerCode,
                    ignored -> new RankingAccumulator(matchPlayer.getPlayer()));
            row.matches += 1;
            if (matchPlayer.getTeamCode().equals(matchPlayer.getMatch().getWinner())) {
                row.wins += 1;
            }
        }
        List<RankingRowDto> ranking = rows.values().stream()
                .map(RankingAccumulator::toDto)
                .sorted(Comparator.comparing(RankingRowDto::getWinRate).reversed()
                        .thenComparing(Comparator.comparing(RankingRowDto::getWins).reversed())
                        .thenComparing(row -> row.getPlayer().getName(), Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        return new RankingResponse(ranking);
    }

    private MatchHistoryDto toHistoryDto(MatchEntity match) {
        String sessionCode = match.getRound().getSession().getSessionCode();
        Integer roundNumber = match.getRound().getRoundNumber();
        Integer courtNumber = match.getCourtNumber();
        List<MatchPlayerEntity> players = scheduleDao.findMatchPlayers(sessionCode, roundNumber, courtNumber);
        MatchHistoryDto dto = new MatchHistoryDto();
        dto.setId(sessionCode + "-" + roundNumber + "-" + courtNumber);
        dto.setSessionCode(sessionCode);
        dto.setRoundNumber(roundNumber);
        dto.setCourtNumber(courtNumber);
        dto.setWinner(match.getWinner());
        dto.setPlayedAt(match.getEndedAt());
        dto.setTeamA(toPlayersByTeam(players, ETeamCode.A));
        dto.setTeamB(toPlayersByTeam(players, ETeamCode.B));
        return dto;
    }

    private List<PlayerDto> toPlayersByTeam(List<MatchPlayerEntity> players, ETeamCode teamCode) {
        return players.stream()
                .filter(player -> teamCode.equals(player.getTeamCode()))
                .map(player -> MapperUtils.convertValue(player.getPlayer(), PlayerDto.class))
                .toList();
    }

    private void completeMatch(String sessionCode, Integer roundNumber, MatchEntity match, ETeamCode winner, Date now) {
        if (Objects.isNull(winner)) {
            throw new IllegalArgumentException("Chưa chọn đội thắng cho sân " + match.getCourtNumber());
        }
        match.setStatus(EMatchStatus.COMPLETED);
        match.setWinner(winner);
        match.setEndedAt(now);
        scheduleDao.saveMatch(match);
        scheduleDao.findMatchPlayers(sessionCode, roundNumber, match.getCourtNumber()).stream()
                .filter(matchPlayer -> !Boolean.FALSE.equals(matchPlayer.getCompleted()))
                .forEach(matchPlayer -> tryReturnPlayerToAvailable(sessionCode, matchPlayer));
    }
    private void tryReturnPlayerToAvailable(String sessionCode, MatchPlayerEntity matchPlayer) {
        String playerCode = matchPlayer.getPlayer().getPlayerCode();
        SessionPlayerEntity sessionPlayer = playSessionDao.getSessionPlayerRequired(sessionCode, playerCode);
        if (canReturnToAvailable(sessionPlayer)) {
            sessionPlayer.setCurrentStatus(ESessionPlayerStatus.AVAILABLE);
            playSessionDao.saveSessionPlayer(sessionPlayer);
        }
    }

    private boolean canReturnToAvailable(SessionPlayerEntity sessionPlayer) {
        return !ESessionPlayerStatus.INJURED.equals(sessionPlayer.getCurrentStatus())
                && !ESessionPlayerStatus.LEFT.equals(sessionPlayer.getCurrentStatus())
                && !ESessionPlayerStatus.TEMP_PAUSED.equals(sessionPlayer.getCurrentStatus())
                && !ESessionPlayerStatus.UNAVAILABLE.equals(sessionPlayer.getCurrentStatus());
    }

    private void validateNoUnfinishedRound(String sessionCode) {
        RoundEntity unfinishedRound = scheduleDao.findLatestRoundByStatuses(
                sessionCode,
                List.of(ERoundStatus.SCHEDULED, ERoundStatus.IN_PROGRESS)
        );
        if (Objects.isNull(unfinishedRound)) {
            return;
        }
        boolean hasUnfinishedMatch = scheduleDao
                .findMatchesByRound(sessionCode, unfinishedRound.getRoundNumber())
                .stream()
                .anyMatch(match -> Objects.isNull(match.getWinner()));
        if (hasUnfinishedMatch) {
            throw new IllegalStateException("Vui lòng chọn đội thắng cho tất cả trận đang diễn ra trước khi tạo trận mới.");
        }
    }

    private int calculatePriority(SessionPlayerEntity player, Integer roundNumber) {
        int restCount = valueOrZero(player.getRestCount());
        int matchCount = valueOrZero(player.getMatchCount());
        int consecutiveCount = valueOrZero(player.getConsecutiveMatchCount());
        int score = 0;
        score += restCount * Constant.ScheduleAlgorithmConfig.REST_COUNT_WEIGHT;
        score -= matchCount * Constant.ScheduleAlgorithmConfig.MATCH_COUNT_WEIGHT;
        score -= consecutiveCount * Constant.ScheduleAlgorithmConfig.CONSECUTIVE_MATCH_WEIGHT;
        if (Objects.nonNull(player.getLastRestRound()) && player.getLastRestRound().equals(roundNumber - 1))
            score += Constant.ScheduleAlgorithmConfig.LAST_ROUND_REST_BONUS;
        return score;
    }

    private List<MatchEntity> createMatches(RoundEntity round, List<SessionPlayerEntity> selectedPlayers, int matchCount,
                                            HistoryIndex historyIndex) {
        SchedulePlan plan = createBestSchedulePlan(selectedPlayers, matchCount, round.getRoundNumber(), historyIndex);
        List<MatchEntity> matches = new ArrayList<>();
        for (int court = 1; court <= plan.matches().size(); court++) {
            MatchPlan matchPlan = plan.matches().get(court - 1);
            PlayerPair pairA = matchPlan.pairA();
            PlayerPair pairB = matchPlan.pairB();
            validateMatchPlayers(pairA, pairB);
            MatchEntity match = createMatch(round, court, pairA, pairB);
            List<MatchPlayerEntity> matchPlayers = createMatchPlayers(match, pairA, pairB);
            scheduleDao.saveMatchPlayers(matchPlayers);
            matches.add(match);
        }
        return matches;
    }

    private SchedulePlan createBestSchedulePlan(List<SessionPlayerEntity> selectedPlayers, int matchCount,
                                                Integer roundNumber, HistoryIndex historyIndex) {
        int attempts = Math.max(80, selectedPlayers.size() * selectedPlayers.size());
        return java.util.stream.IntStream.range(0, attempts)
                .mapToObj(ignored -> createRandomSchedulePlan(selectedPlayers, matchCount, roundNumber, historyIndex))
                .min(Comparator.comparing(SchedulePlan::penalty))
                .orElseThrow(() -> new IllegalStateException("Không tạo được phương án xếp trận hợp lệ"));
    }

    private SchedulePlan createRandomSchedulePlan(List<SessionPlayerEntity> selectedPlayers, int matchCount,
                                                  Integer roundNumber, HistoryIndex historyIndex) {
        List<SessionPlayerEntity> pool = new ArrayList<>(selectedPlayers);
        Collections.shuffle(pool);
        List<MatchPlan> matchPlans = new ArrayList<>();
        SchedulePenalty totalPenalty = SchedulePenalty.zero();
        int playersPerMatch = Constant.ScheduleAlgorithmConfig.PLAYERS_PER_DOUBLES_MATCH;
        for (int matchIndex = 0; matchIndex < matchCount; matchIndex++) {
            int fromIndex = matchIndex * playersPerMatch;
            int toIndex = fromIndex + playersPerMatch;
            if (toIndex > pool.size())
                throw new IllegalStateException("Không đủ người chơi để tạo trận");
            MatchPlan matchPlan = createBestMatchPlan(pool.subList(fromIndex, toIndex), roundNumber, historyIndex);
            matchPlans.add(matchPlan);
            totalPenalty = totalPenalty.add(matchPlan.penalty());
        }
        return new SchedulePlan(matchPlans, totalPenalty);
    }

    private MatchPlan createBestMatchPlan(List<SessionPlayerEntity> players, Integer roundNumber, HistoryIndex historyIndex) {
        if (players.size() != Constant.ScheduleAlgorithmConfig.PLAYERS_PER_DOUBLES_MATCH)
            throw new IllegalStateException("Một trận đánh đôi phải có đúng 4 người chơi");
        List<MatchPlan> candidates = new ArrayList<>(List.of(
                createMatchPlan(players.get(0), players.get(1), players.get(2), players.get(3), roundNumber, historyIndex),
                createMatchPlan(players.get(0), players.get(2), players.get(1), players.get(3), roundNumber, historyIndex),
                createMatchPlan(players.get(0), players.get(3), players.get(1), players.get(2), roundNumber, historyIndex)
        ));
        Collections.shuffle(candidates);
        return candidates.stream()
                .min(Comparator.comparing(MatchPlan::penalty))
                .orElseThrow(() -> new IllegalStateException("Không tạo được cách chia đội hợp lệ"));
    }

    private MatchPlan createMatchPlan(SessionPlayerEntity a1, SessionPlayerEntity a2,
                                      SessionPlayerEntity b1, SessionPlayerEntity b2,
                                      Integer roundNumber, HistoryIndex historyIndex) {
        PlayerPair pairA = new PlayerPair(a1, a2);
        PlayerPair pairB = new PlayerPair(b1, b2);
        validateMatchPlayers(pairA, pairB);
        return new MatchPlan(pairA, pairB, calculateMatchPenalty(pairA, pairB, roundNumber, historyIndex));
    }
    private void validateMatchPlayers(PlayerPair pairA, PlayerPair pairB) {
        List<SessionPlayerEntity> players = List.of(pairA.first(), pairA.second(), pairB.first(), pairB.second());
        Set<Long> uniquePlayerIds = players.stream()
                .map(this::getPlayerId)
                .collect(Collectors.toSet());
        if (uniquePlayerIds.size() != Constant.ScheduleAlgorithmConfig.PLAYERS_PER_DOUBLES_MATCH)
            throw new IllegalStateException("Không thể tạo trận vì không đủ 4 người chơi khác nhau");
    }

    private MatchEntity createMatch(RoundEntity round, Integer courtNumber,
                                    PlayerPair pairA, PlayerPair pairB) {
        BigDecimal totalA = pairA.totalScore();
        BigDecimal totalB = pairB.totalScore();
        BigDecimal difference = totalA.subtract(totalB).abs();
        MatchEntity match = new MatchEntity();
        match.setRound(round);
        match.setCourtNumber(courtNumber);
        match.setStatus(EMatchStatus.SCHEDULED);
        match.setTotalScoreA(totalA);
        match.setTotalScoreB(totalB);
        match.setScoreDifference(difference);
        return scheduleDao.saveMatch(match);
    }

    private SchedulePenalty calculateMatchPenalty(PlayerPair pairA, PlayerPair pairB, Integer roundNumber, HistoryIndex historyIndex) {
        BigDecimal levelDifference = pairA.totalScore()
                .subtract(pairB.totalScore())
                .abs();
        return new SchedulePenalty(
                levelDifference,
                teamMatchCountDifference(pairA, pairB),
                teamRestDurationDifference(pairA, pairB, roundNumber),
                countPartnerRepeat(pairA, pairB, historyIndex),
                countOpponentRepeat(pairA, pairB, historyIndex)
        );
    }

    private int teamMatchCountDifference(PlayerPair pairA, PlayerPair pairB) {
        return Math.abs(totalMatchCount(pairA) - totalMatchCount(pairB));
    }

    private int totalMatchCount(PlayerPair pair) {
        return valueOrZero(pair.first().getMatchCount()) + valueOrZero(pair.second().getMatchCount());
    }

    private int teamRestDurationDifference(PlayerPair pairA, PlayerPair pairB, Integer roundNumber) {
        return Math.abs(totalRestDuration(pairA, roundNumber) - totalRestDuration(pairB, roundNumber));
    }

    private int totalRestDuration(PlayerPair pair, Integer roundNumber) {
        return restDuration(pair.first(), roundNumber) + restDuration(pair.second(), roundNumber);
    }

    private int restDuration(SessionPlayerEntity player, Integer roundNumber) {
        if (Objects.isNull(player.getLastPlayedRound())) {
            return roundNumber;
        }
        return Math.max(0, roundNumber - player.getLastPlayedRound());
    }
    private int countPartnerRepeat(PlayerPair pairA, PlayerPair pairB, HistoryIndex historyIndex) {
        return historyIndex.getPartnerCount(pairA.first().getPlayer().getPlayerCode(), pairA.second().getPlayer().getPlayerCode())
                + historyIndex.getPartnerCount(pairB.first().getPlayer().getPlayerCode(), pairB.second().getPlayer().getPlayerCode());
    }
    private int countOpponentRepeat(PlayerPair pairA, PlayerPair pairB, HistoryIndex historyIndex) {
        List<SessionPlayerEntity> teamA = List.of(pairA.first(), pairA.second());
        List<SessionPlayerEntity> teamB = List.of(pairB.first(), pairB.second());
        return teamA.stream()
                .flatMap(a -> teamB.stream().map(b -> historyIndex.getOpponentCount(
                        a.getPlayer().getPlayerCode(),
                        b.getPlayer().getPlayerCode()
                )))
                .mapToInt(Integer::intValue)
                .sum();
    }

    private List<MatchPlayerEntity> createMatchPlayers(MatchEntity match, PlayerPair pairA, PlayerPair pairB) {
        List<SessionPlayerEntity> sessionPlayers = List.of(pairA.first(), pairA.second(), pairB.first(), pairB.second());
        Set<String> playerCodes = sessionPlayers.stream()
                .map(this::getPlayerCode)
                .collect(Collectors.toSet());
        if (playerCodes.size() != Constant.ScheduleAlgorithmConfig.PLAYERS_PER_DOUBLES_MATCH)
            throw new IllegalStateException("Một trận đánh đôi phải có 4 người chơi khác nhau: " + playerCodes);
        return List.of(
                createMatchPlayer(match, pairA.first().getPlayer(), ETeamCode.A),
                createMatchPlayer(match, pairA.second().getPlayer(), ETeamCode.A),
                createMatchPlayer(match, pairB.first().getPlayer(), ETeamCode.B),
                createMatchPlayer(match, pairB.second().getPlayer(), ETeamCode.B));
    }

    private MatchPlayerEntity createMatchPlayer(MatchEntity match, PlayerEntity player, ETeamCode teamCode) {
        MatchPlayerEntity entity = new MatchPlayerEntity();
        entity.setMatch(match);
        entity.setPlayer(player);
        entity.setTeamCode(teamCode);
        entity.setRole(EMatchPlayerRole.MAIN);
        entity.setCompleted(true);
        entity.setJoinedAt(new Date());
        return entity;
    }

    private void updateSessionPlayersAfterSchedule(List<SessionPlayerEntity> selectedPlayers,
                                                   List<SessionPlayerEntity> restingPlayers, Integer roundNumber) {
        selectedPlayers.forEach(player -> markPlayerAsPlaying(player, roundNumber));
        restingPlayers.forEach(player -> markPlayerAsResting(player, roundNumber));
    }

    private void markPlayerAsPlaying(SessionPlayerEntity player, Integer roundNumber) {
        player.setCurrentStatus(ESessionPlayerStatus.PLAYING);
        player.setMatchCount(valueOrZero(player.getMatchCount()) + 1);
        player.setConsecutiveMatchCount(valueOrZero(player.getConsecutiveMatchCount()) + 1);
        player.setLastPlayedRound(roundNumber);
        playSessionDao.saveSessionPlayer(player);
    }

    private void markPlayerAsResting(SessionPlayerEntity player, Integer roundNumber) {
        player.setCurrentStatus(ESessionPlayerStatus.RESTING);
        player.setRestCount(valueOrZero(player.getRestCount()) + 1);
        player.setConsecutiveMatchCount(0);
        player.setLastRestRound(roundNumber);
        playSessionDao.saveSessionPlayer(player);
    }

    private HistoryIndex buildHistoryIndex(String sessionCode) {
        List<MatchPlayerEntity> matchPlayers = scheduleDao.findSessionMatchPlayers(sessionCode);
        Map<String, List<MatchPlayerEntity>> byMatch = matchPlayers.stream()
                .filter(matchPlayer -> !Boolean.FALSE.equals(matchPlayer.getCompleted()))
                .collect(Collectors.groupingBy(this::matchKey));
        HistoryIndex index = new HistoryIndex();
        byMatch.values().forEach(players -> registerMatchHistory(players, index));
        return index;
    }

    private String matchKey(MatchPlayerEntity matchPlayer) {
        MatchEntity match = matchPlayer.getMatch();
        return match.getRound().getRoundNumber() + "_" + match.getCourtNumber();
    }

    private void registerMatchHistory(List<MatchPlayerEntity> players, HistoryIndex index) {
        List<MatchPlayerEntity> teamA = players.stream()
                .filter(player -> ETeamCode.A.equals(player.getTeamCode())).toList();
        List<MatchPlayerEntity> teamB = players.stream()
                .filter(player -> ETeamCode.B.equals(player.getTeamCode())).toList();
        increasePartnerHistory(teamA, index);
        increasePartnerHistory(teamB, index);
        increaseOpponentHistory(teamA, teamB, index);
    }

    private void increasePartnerHistory(List<MatchPlayerEntity> team, HistoryIndex index) {
        if (team.size() < 2) return;
        String first = team.get(0).getPlayer().getPlayerCode();
        String second = team.get(1).getPlayer().getPlayerCode();
        index.increasePartner(first, second);
    }

    private void increaseOpponentHistory(List<MatchPlayerEntity> teamA, List<MatchPlayerEntity> teamB, HistoryIndex index) {
        teamA.forEach(a -> teamB.forEach(b -> index.increaseOpponent(
                a.getPlayer().getPlayerCode(), b.getPlayer().getPlayerCode())));
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
        return matches.stream()
                .map(this::toMatchDto)
                .toList();
    }

    private MatchDto toMatchDto(MatchEntity match) {
        String sessionCode = match.getRound().getSession().getSessionCode();
        Integer roundNumber = match.getRound().getRoundNumber();
        Integer courtNumber = match.getCourtNumber();
        List<MatchPlayerEntity> matchPlayers = scheduleDao.findMatchPlayers(sessionCode, roundNumber, courtNumber);
        return buildMatchDto(match, sessionCode, roundNumber, courtNumber, matchPlayers);
    }

    private record SchedulePenalty(
            BigDecimal levelDifference,
            int matchCountDifference,
            int restDurationDifference,
            int partnerRepeat,
            int opponentRepeat
    ) implements Comparable<SchedulePenalty> {
        static SchedulePenalty zero() {
            return new SchedulePenalty(BigDecimal.ZERO, 0, 0, 0, 0);
        }

        SchedulePenalty add(SchedulePenalty other) {
            return new SchedulePenalty(
                    levelDifference.add(other.levelDifference),
                    matchCountDifference + other.matchCountDifference,
                    restDurationDifference + other.restDurationDifference,
                    partnerRepeat + other.partnerRepeat,
                    opponentRepeat + other.opponentRepeat
            );
        }

        @Override
        public int compareTo(SchedulePenalty other) {
            int levelCompare = levelDifference.compareTo(other.levelDifference);
            if (levelCompare != 0) return levelCompare;
            int matchCountCompare = Integer.compare(matchCountDifference, other.matchCountDifference);
            if (matchCountCompare != 0) return matchCountCompare;
            int restCompare = Integer.compare(restDurationDifference, other.restDurationDifference);
            if (restCompare != 0) return restCompare;
            int partnerCompare = Integer.compare(partnerRepeat, other.partnerRepeat);
            if (partnerCompare != 0) return partnerCompare;
            return Integer.compare(opponentRepeat, other.opponentRepeat);
        }
    }
    private record SchedulePlan(List<MatchPlan> matches, SchedulePenalty penalty) {
    }

    private record MatchPlan(PlayerPair pairA, PlayerPair pairB, SchedulePenalty penalty) {
    }

    private record PlayerPair(SessionPlayerEntity first, SessionPlayerEntity second) {
        BigDecimal totalScore() {
            return resolveLevelScore(first.getPlayer()).add(resolveLevelScore(second.getPlayer()));
        }
    }


    private static class RankingAccumulator {
        private final PlayerEntity player;
        private int matches;
        private int wins;

        private RankingAccumulator(PlayerEntity player) {
            this.player = player;
        }

        private RankingRowDto toDto() {
            RankingRowDto dto = new RankingRowDto();
            dto.setPlayer(MapperUtils.convertValue(player, PlayerDto.class));
            dto.setMatches(matches);
            dto.setWins(wins);
            dto.setWinRate(matches == 0 ? 0 : Math.round((wins * 100f) / matches));
            return dto;
        }
    }
    private static class HistoryIndex {
        private final Map<String, Integer> partnerHistory = new HashMap<>();
        private final Map<String, Integer> opponentHistory = new HashMap<>();

        void increasePartner(String a, String b) {
            partnerHistory.merge(key(a, b), 1, Integer::sum);
        }

        void increaseOpponent(String a, String b) {
            opponentHistory.merge(key(a, b), 1, Integer::sum);
        }

        int getPartnerCount(String a, String b) {
            return partnerHistory.getOrDefault(key(a, b), 0);
        }

        int getOpponentCount(String a, String b) {
            return opponentHistory.getOrDefault(key(a, b), 0);
        }

        private String key(String a, String b) {
            return a.compareTo(b) <= 0 ? a + "_" + b : b + "_" + a;
        }
    }

    private static BigDecimal resolveLevelScore(PlayerEntity player) {
        return Objects.nonNull(player.getLevelScore())
                ? player.getLevelScore()
                : Objects.nonNull(player.getLevel()) ? player.getLevel().getAverageScore() : BigDecimal.ZERO;
    }

    private String getPlayerCode(SessionPlayerEntity sessionPlayer) {
        return sessionPlayer.getPlayer().getPlayerCode();
    }

    private List<SessionPlayerEntity> distinctSessionPlayersByPlayerId(List<SessionPlayerEntity> players) {
        Map<Long, SessionPlayerEntity> uniquePlayers = new LinkedHashMap<>();
        for (SessionPlayerEntity player : players) {
            if (Objects.isNull(player) || Objects.isNull(player.getPlayer()) || Objects.isNull(player.getPlayer().getId()))
                continue;
            uniquePlayers.putIfAbsent(player.getPlayer().getId(), player);
        }
        return new ArrayList<>(uniquePlayers.values());
    }

    private List<SessionPlayerEntity> selectPlayers(List<SessionPlayerEntity> players, int playerNeeded, Integer roundNumber) {
        List<SessionPlayerEntity> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);
        return shuffledPlayers.stream()
                .sorted(Comparator.comparingInt((SessionPlayerEntity player) -> valueOrZero(player.getMatchCount()))
                        .thenComparing(Comparator.comparingInt((SessionPlayerEntity player) -> restDuration(player, roundNumber)).reversed())
                        .thenComparingInt(player -> valueOrZero(player.getConsecutiveMatchCount())))
                .limit(playerNeeded)
                .toList();
    }

    private Long getPlayerId(SessionPlayerEntity sessionPlayer) {
        if (Objects.isNull(sessionPlayer) || Objects.isNull(sessionPlayer.getPlayer()) || Objects.isNull(sessionPlayer.getPlayer().getId()))
            throw new IllegalStateException("SessionPlayer không có thông tin người chơi hợp lệ");
        return sessionPlayer.getPlayer().getId();
    }
}