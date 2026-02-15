package com.schedulemng.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "work_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 날짜
    @Column(name = "date", nullable = false, length = 10)
    private String date;

    // 담당자
    @Column(name = "manager", nullable = false, length = 10)
    private String manager;

    // 현금결제
    @Lob
    @Column(name = "cashPayment", length = 50)
    private String cashPayment;

    // 지정석, 특수예약, 멥버쉽
    @Lob
    @Column(name = "reservations", length = 300)
    private String reservations;

    // 이벤트
    @Lob
    @Column(name = "event", length = 300)
    private String event;

    // 가게관련
    @Lob
    @Column(name = "storeRelated", length = 300)
    private String storeRelated;

    // 특이사항
    @Lob
    @Column(name = "notes", length = 300)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

} 