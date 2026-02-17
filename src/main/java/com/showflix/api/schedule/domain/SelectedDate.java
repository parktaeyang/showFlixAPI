package com.showflix.api.schedule.domain;

/**
 * Domain Layer - 선택된 날짜(출근일) 도메인 모델
 */
public class SelectedDate {

    private String date;
    private String userId;
    private String userName;
    private boolean openHope;
    private String role;
    private String confirmed;
    private String remarks;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isOpenHope() {
        return openHope;
    }

    public void setOpenHope(boolean openHope) {
        this.openHope = openHope;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(String confirmed) {
        this.confirmed = confirmed;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
