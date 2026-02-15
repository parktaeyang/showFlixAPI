package com.schedulemng.dto;

import com.schedulemng.entity.ScheduleSpecial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSpecialDto {
    
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
    
    // 엔티티를 DTO로 변환
    public static ScheduleSpecialDto fromEntity(ScheduleSpecial entity) {
        if (entity == null) {
            return null;
        }
        
        return ScheduleSpecialDto.builder()
                .id(entity.getId())
                .reservationDate(entity.getReservationDate())
                .reservationTime(entity.getReservationTime())
                .specialRemarks(entity.getSpecialRemarks())
                .customerName(entity.getCustomerName())
                .peopleCount(entity.getPeopleCount())
                .paymentStatus(entity.getPaymentStatus())
                .contactInfo(entity.getContactInfo())
                .notes(entity.getNotes())
                .reservationStatus(entity.getReservationStatus() != null ? entity.getReservationStatus().name() : null)
                .highlightType(entity.getHighlightType() != null ? entity.getHighlightType().name() : null)
                .expectedRevenue(entity.getExpectedRevenue())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
    
    // DTO를 엔티티로 변환
    public ScheduleSpecial toEntity() {
        return ScheduleSpecial.builder()
                .id(this.id)
                .reservationDate(this.reservationDate)
                .reservationTime(this.reservationTime)
                .specialRemarks(this.specialRemarks)
                .customerName(this.customerName)
                .peopleCount(this.peopleCount)
                .paymentStatus(this.paymentStatus)
                .contactInfo(this.contactInfo)
                .notes(this.notes)
                .reservationStatus(this.reservationStatus != null ? 
                    ScheduleSpecial.ReservationStatus.valueOf(this.reservationStatus) : 
                    ScheduleSpecial.ReservationStatus.PENDING)
                .highlightType(this.highlightType != null ? 
                    ScheduleSpecial.HighlightType.valueOf(this.highlightType) : 
                    ScheduleSpecial.HighlightType.NONE)
                .expectedRevenue(this.expectedRevenue)
                .createdBy(this.createdBy)
                .updatedBy(this.updatedBy)
                .build();
    }
}
