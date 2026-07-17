package com.huy.b7n.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.huy.b7n.dto.MatchHistoryDto;
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
public class HistoryResponse {
    private List<MatchHistoryDto> history;
}