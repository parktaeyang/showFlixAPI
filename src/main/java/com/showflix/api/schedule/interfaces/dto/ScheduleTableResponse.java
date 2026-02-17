package com.showflix.api.schedule.interfaces.dto;

import java.util.List;
import java.util.Map;

/**
 * Interfaces Layer - 스케줄 테이블 전체 응답 DTO
 * actorNames: 배우 이름 목록 (열 순서 정의)
 * columnTotals: 배우별 월 총시간
 * grandTotal: 전체 월 총시간
 */
public record ScheduleTableResponse(
        int year,
        int month,
        List<String> actorNames,
        List<ScheduleRowResponse> rows,
        Map<String, String> columnTotals,
        String grandTotal
) {}
