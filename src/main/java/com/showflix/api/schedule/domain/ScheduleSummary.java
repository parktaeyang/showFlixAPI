package com.showflix.api.schedule.domain;

/**
 * Domain Layer - 출근시간 요약 애그리게이트
 * schedule_summary 테이블 매핑
 */
public class ScheduleSummary {

    private Long id;
    private String userId;
    private String date;    // YYYY-MM-DD
    private String hours;   // 출근 시간 (소수점 1자리 허용, 예: "8", "7.5")
    private String remarks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getHours() { return hours; }
    public void setHours(String hours) { this.hours = hours; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
