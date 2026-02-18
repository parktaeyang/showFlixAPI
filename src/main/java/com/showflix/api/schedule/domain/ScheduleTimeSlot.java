package com.showflix.api.schedule.domain;

/**
 * 시간표 슬롯 도메인 모델
 * schedule_time_slot 테이블 매핑
 * 복합키: scheduleDate + timeSlot
 */
public class ScheduleTimeSlot {

    private String scheduleDate;  // YYYY-MM-DD
    private String timeSlot;      // HH:mm
    private String theme;
    private String performer;     // 콤마 구분 출연자 목록
    private String confirmed;     // Y / N

    public ScheduleTimeSlot() {}

    public String getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(String scheduleDate) { this.scheduleDate = scheduleDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getPerformer() { return performer; }
    public void setPerformer(String performer) { this.performer = performer; }

    public String getConfirmed() { return confirmed; }
    public void setConfirmed(String confirmed) { this.confirmed = confirmed; }
}
