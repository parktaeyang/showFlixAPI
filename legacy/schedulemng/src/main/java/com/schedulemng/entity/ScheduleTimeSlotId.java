package com.schedulemng.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // 기본 생성자 추가 (선택 사항이지만 @Embeddable에서 유용)
import lombok.AllArgsConstructor;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleTimeSlotId implements Serializable {

    @Column(name = "schedule_date", nullable = false, length = 10)
    private String scheduleDate; // "2025-09-02" 형식으로 저장

    @Column(name = "time_slot", nullable = false, length = 5)
    private String timeSlot; // "16:00" 형식으로 저장

    // equals() and hashCode() 오버라이드 필수 (Lombok으로도 생성 가능하나 명시적 작성 권장)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleTimeSlotId that = (ScheduleTimeSlotId) o;
        return Objects.equals(scheduleDate, that.scheduleDate) &&
                Objects.equals(timeSlot, that.timeSlot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleDate, timeSlot);
    }
}
