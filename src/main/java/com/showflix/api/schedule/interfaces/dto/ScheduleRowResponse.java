package com.showflix.api.schedule.interfaces.dto;

import java.util.Map;

/**
 * Interfaces Layer - 스케줄 테이블 행 응답 DTO
 * actorHours: 배우명 → 근무시간 문자열 (빈 문자열이면 해당 날짜 미출근)
 */
public record ScheduleRowResponse(
        String date,
        String dayOfWeek,
        Map<String, String> actorHours,
        String rowTotal,
        String remarks
) {}
