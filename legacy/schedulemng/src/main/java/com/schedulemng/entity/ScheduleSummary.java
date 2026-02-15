package com.schedulemng.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;
    
    @Column(name = "date", nullable = false, length = 10)
    private String date;
    
    @Column(name = "hours", length = 10)
    private String hours = "0";
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 편의 메서드
    public ScheduleSummary(String userId, String date, String hours) {
        this.userId = userId;
        this.date = date;
        this.hours = hours;
    }
    
    public ScheduleSummary(String userId, String date, String hours, String remarks) {
        this.userId = userId;
        this.date = date;
        this.hours = hours;
        this.remarks = remarks;
    }
} 