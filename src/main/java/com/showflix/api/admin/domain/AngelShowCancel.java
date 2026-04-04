package com.showflix.api.admin.domain;

/**
 * Domain Layer - 엔젤쇼 취소현황 도메인 모델
 * 테이블: sf_angel_show_cancel
 */
public class AngelShowCancel {

    private Long id;
    private String cancelDate;
    private String showTime;
    private String reason;
    private String actorName;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCancelDate() { return cancelDate; }
    public void setCancelDate(String cancelDate) { this.cancelDate = cancelDate; }

    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
