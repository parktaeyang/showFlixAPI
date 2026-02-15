package com.schedulemng.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(SelectedDateId.class)
@Table(name = "selected_date")
public class SelectedDate {

    @Id
    @Column(nullable = false, name = "date")
    private String date;

    @Id
    @Column(nullable = false, name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "open_hope")
    private boolean openHope;

    @Column(name = "role")
    private String role;

    @Column(name = "confirmed", length = 1)
    private String confirmed = "N"; // 기본값: 미확정

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks; // 사용자별 비고
}