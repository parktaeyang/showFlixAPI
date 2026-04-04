package com.showflix.api.admin.domain;

/**
 * Domain Layer - 일일현황 항목
 * sf_selected_date + sf_schedule 조합 읽기 전용 뷰 모델
 */
public class DailyStatusEntry {

    private String userId;
    private String userName;
    private String role;
    private String confirmed;
    private boolean openHope;
    private String remarks;
    private String accountType;
    private Double hours;
    private String scheduleMemo;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getConfirmed() { return confirmed; }
    public void setConfirmed(String confirmed) { this.confirmed = confirmed; }

    public boolean isOpenHope() { return openHope; }
    public void setOpenHope(boolean openHope) { this.openHope = openHope; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }

    public String getScheduleMemo() { return scheduleMemo; }
    public void setScheduleMemo(String scheduleMemo) { this.scheduleMemo = scheduleMemo; }
}
