package com.schedulemng.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_special")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSpecial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "reservation_date", nullable = false, length = 50)
    private String reservationDate;
    
    @Column(name = "reservation_time", length = 20)
    private String reservationTime;
    
    @Column(name = "special_remarks", columnDefinition = "TEXT")
    private String specialRemarks;
    
    @Column(name = "customer_name", length = 100)
    private String customerName;
    
    @Column(name = "people_count")
    private Integer peopleCount;
    
    @Column(name = "payment_status", columnDefinition = "TEXT")
    private String paymentStatus;
    
    @Column(name = "contact_info", length = 50)
    private String contactInfo;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus reservationStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "highlight_type")
    private HighlightType highlightType;
    
    @Column(name = "expected_revenue")
    private Long expectedRevenue;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    // 예약 상태 열거형
    public enum ReservationStatus {
        PENDING("대기"),
        CONFIRMED("확정"),
        COMPLETED("완료"),
        CANCELLED("취소");
        
        private final String displayName;
        
        ReservationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 하이라이트 타입 열거형
    public enum HighlightType {
        NONE("없음"),
        GREEN("초록"),
        YELLOW("노랑"),
        BLUE("파랑"),
        RED("빨강");
        
        private final String displayName;
        
        HighlightType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
