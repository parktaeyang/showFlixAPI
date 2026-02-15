package com.schedulemng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSpecialRequestDto {
    
    @NotBlank(message = "예약 날짜는 필수입니다.")
    private String reservationDate;
    
    private String reservationTime;
    private String specialRemarks;
    private String customerName;
    
    @Min(value = 1, message = "인원수는 1명 이상이어야 합니다.")
    private Integer peopleCount;
    
    private String paymentStatus;
    private String contactInfo;
    private String notes;
    private String reservationStatus;
    private String highlightType;
    private Long expectedRevenue;
    private String createdBy;
    private String updatedBy;
}
