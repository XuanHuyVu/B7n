package com.huy.b7n.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.huy.b7n.common.ETeamCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteRoundRequest {
    private String sessionCode;
    private Integer roundNumber;
    private List<MatchResultRequest> results;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatchResultRequest {
        private Integer courtNumber;
        private ETeamCode winner;
    }
}