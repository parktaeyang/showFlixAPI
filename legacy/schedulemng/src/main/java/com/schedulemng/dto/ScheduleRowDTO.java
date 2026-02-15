package com.schedulemng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRowDTO {
    private String date;
    private String dayOfWeek;
    private Map<String, String> actorHours; // 배우별 시간
    private String rowTotal; // 해당 날짜의 총 시간
    private String remarks; // 특이사항
} 