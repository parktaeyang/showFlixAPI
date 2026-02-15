package com.schedulemng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSpecialResponseDto {
    
    private Long id;
    private String reservationDate;
    private String reservationTime;
    private String specialRemarks;
    private String customerName;
    private Integer peopleCount;
    private String paymentStatus;
    private String contactInfo;
    private String notes;
    private String reservationStatus;
    private String highlightType;
    private Long expectedRevenue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // 통계 정보를 위한 응답 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsResponse {
        private long totalReservations;
        private long pendingReservations;
        private long confirmedReservations;
        private long completedReservations;
        private long cancelledReservations;
        private long totalExpectedRevenue;
        private long pendingRevenue;
        private long confirmedRevenue;
        private long completedRevenue;
    }
    
    // 페이지네이션을 위한 응답 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagedResponse {
        private List<ScheduleSpecialResponseDto> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
