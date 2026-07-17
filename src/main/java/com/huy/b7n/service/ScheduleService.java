package com.huy.b7n.service;

import com.huy.b7n.request.CompleteRoundRequest;
import com.huy.b7n.request.GenerateNextRoundRequest;
import com.huy.b7n.response.GenerateRoundResponse;
import com.huy.b7n.response.HistoryResponse;
import com.huy.b7n.response.RankingResponse;
import org.springframework.transaction.annotation.Transactional;

public interface ScheduleService {

    @Transactional
    GenerateRoundResponse generateNextRound(GenerateNextRoundRequest request);

    @Transactional
    GenerateRoundResponse completeRound(CompleteRoundRequest request);

    HistoryResponse getHistory(String sessionCode);

    RankingResponse getRanking(String sessionCode);
}