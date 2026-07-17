package com.huy.b7n.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.huy.b7n.common.ETeamCode;
import com.huy.b7n.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchHistoryDto {
    private String id;
    private String sessionCode;
    private Integer roundNumber;
    private Integer courtNumber;
    private ETeamCode winner;
    private List<PlayerDto> teamA;
    private List<PlayerDto> teamB;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.NORMAL_TIME_PATTERN, timezone = DateUtils.DEFAULT_TIMEZONE_GMT7)
    private Date playedAt;
}