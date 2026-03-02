package com.showflix.api.schedule.domain;

/**
 * Domain Layer - 관리자 공지사항 도메인 클래스
 */
public class AdminNote {

    private String id;
    private String content;
    private String updatedBy;
    private String updatedAt;

    public AdminNote() {}

    public AdminNote(String id, String content, String updatedBy, String updatedAt) {
        this.id = id;
        this.content = content;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
