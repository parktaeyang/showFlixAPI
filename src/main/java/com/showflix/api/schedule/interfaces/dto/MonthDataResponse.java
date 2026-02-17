package com.showflix.api.schedule.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Interfaces Layer - 월별 데이터 응답 DTO
 */
public record MonthDataResponse(
        @JsonProperty("isAdmin") boolean admin,
        List<SelectedDateResponse> data
) {}
