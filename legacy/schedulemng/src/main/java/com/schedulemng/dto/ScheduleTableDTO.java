package com.schedulemng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleTableDTO {
    private int year;
    private int month;
    private List<String> actorNames; // 배우 이름 목록
    private List<ScheduleRowDTO> rows; // 날짜별 행 데이터
    private Map<String, String> columnTotals; // 배우별 총 시간
    private String grandTotal; // 전체 총 시간
} 