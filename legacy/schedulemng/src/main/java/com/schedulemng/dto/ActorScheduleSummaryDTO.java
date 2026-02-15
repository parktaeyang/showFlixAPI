package com.schedulemng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorScheduleSummaryDTO {
    private String userid;
    private String username;
    private String phoneNumber;
    private int totalDays; // 해당 월에 신청한 총 일수
    private double totalHours; // 해당 월에 신청한 총 시간 (시간 단위로 계산)
    private String month; // 조회 월 (YYYY-MM 형식)
} 