package com.showflix.api.admin.domain;

/**
 * Domain Layer - 보건증 관리 도메인 모델
 * 테이블: sf_health_cert
 */
public class HealthCert {

    private String userId;
    private String userName;
    private String expireDate;
    private String notes;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getExpireDate() { return expireDate; }
    public void setExpireDate(String expireDate) { this.expireDate = expireDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
