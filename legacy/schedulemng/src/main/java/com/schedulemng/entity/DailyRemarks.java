package com.schedulemng.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "daily_remarks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyRemarks {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "date", nullable = false, unique = true, length = 10)
    private String date;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 편의 메서드
    public DailyRemarks(String date, String remarks) {
        this.date = date;
        this.remarks = remarks;
    }
} 