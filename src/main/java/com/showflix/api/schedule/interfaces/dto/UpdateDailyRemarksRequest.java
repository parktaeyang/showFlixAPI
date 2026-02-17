package com.showflix.api.schedule.interfaces.dto;

/**
 * Interfaces Layer - 일별 특이사항 업데이트 요청 DTO
 */
public class UpdateDailyRemarksRequest {

    private String date;
    private String remarks;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
