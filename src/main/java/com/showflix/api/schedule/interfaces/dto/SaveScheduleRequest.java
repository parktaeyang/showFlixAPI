package com.showflix.api.schedule.interfaces.dto;

/**
 * Interfaces Layer - 스케줄 저장/수정 요청 DTO
 */
public class SaveScheduleRequest {

    private String date;
    private String username;
    private Double hours;
    private String remarks;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
