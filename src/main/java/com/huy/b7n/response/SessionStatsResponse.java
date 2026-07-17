package com.huy.b7n.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.huy.b7n.dto.SessionPlayerStatDto;
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
public class SessionStatsResponse {
    private String sessionCode;
    private List<SessionPlayerStatDto> players;
}