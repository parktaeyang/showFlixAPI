package com.showflix.api.schedule.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interfaces Layer - 선택 날짜 응답 DTO
 */
public record SelectedDateResponse(
        String date,
        String userId,
        String userName,
        @JsonProperty("openHope") boolean openHope,
        String role,
        String confirmed,
        String remarks
) {}
