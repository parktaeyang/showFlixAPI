package com.showflix.api.schedule.domain;

/**
 * Domain Layer - Schedule 도메인 모델 (근무시간 레코드)
 * DB 테이블: schedule
 * 복합 유니크: date + username
 */
public class Schedule {

    private Long id;
    private String date;       // "yyyy-MM-dd" 형식
    private String username;   // 배우 이름 (schedule_users.username 참조)
    private Double hours;      // 근무시간
    private String memo;       // 메모
    private String remarks;    // 일별 특이사항 (해당 날짜 전체 공유)

    public Schedule() {}

    public Schedule(Long id, String date, String username,
                    Double hours, String memo, String remarks) {
        this.id = id;
        this.date = date;
        this.username = username;
        this.hours = hours;
        this.memo = memo;
        this.remarks = remarks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
