package com.schedulemng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyScheduleDTO {
    private String date;
    private String dayOfWeek;
    private Map<String, Double> actorSchedules; // 배우별 시간
    private double dailyTotal; // 해당 일의 총 시간
    private String remarks; // 특이사항
} 