package com.showflix.api.admin.domain;

/**
 * Domain Layer - 연간일정캘린더 항목
 * sf_time_slot 테이블 기반 읽기 전용 뷰 모델
 */
public class AnnualCalendarEntry {

    private String scheduleDate;
    private String timeSlot;
    private String theme;
    private String performer;
    private String confirmed;

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
