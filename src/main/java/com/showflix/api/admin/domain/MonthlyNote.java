package com.showflix.api.admin.domain;

public class MonthlyNote {
    private int year;
    private int month;
    private String content;
    private String updatedBy;

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
