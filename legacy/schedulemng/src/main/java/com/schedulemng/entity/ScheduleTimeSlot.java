package com.schedulemng.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // 기본 생성자 추가 (JPA 필수)

@Entity
@Table(name = "schedule_time_slot")
@Getter
@Setter
@NoArgsConstructor
public class ScheduleTimeSlot {

    @EmbeddedId // 복합 기본 키 사용
    private ScheduleTimeSlotId id;

    @Column(name = "theme")
    private String theme;

    @Column(name = "performer")
    private String performer;

    @Column(name = "confirmed", length = 1)
    private String confirmed = "N"; // 기본값: N (미확정)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}