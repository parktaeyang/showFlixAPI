package com.schedulemng.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkLogDTO {
    private Long id;                // ID
    @NotBlank(message = "날짜는 필수 입력 항목입니다.")
    private String date;            // 날짜
    private String manager;         // 담당자
    private String cashPayment;     // 현금결제
    private String reservations;    // 지정석&특수예약&멤버쉽
    private String event;           // 이벤트
    private String storeRelated;    // 가게관련
    private String notes;           // 특이사항
} 