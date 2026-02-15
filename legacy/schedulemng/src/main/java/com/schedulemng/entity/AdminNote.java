package com.schedulemng.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "admin_note")
public class AdminNote {

    @Id
    @Column(nullable = false, name = "id")
    private String id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private String updatedAt;


}
