package com.showflix.api.schedule.domain;

/**
 * Domain Layer - 특수예약 도메인 모델
 * 테이블: schedule_special
 */
public class ScheduleSpecial {

    private Long id;
    private String reservationDate;   // 예약 날짜 (YYYY-MM-DD)
    private String reservationTime;   // 예약 시간 (HH:mm)
    private String customerName;      // 예약자명
    private Integer peopleCount;      // 인원수
    private String contactInfo;       // 연락처
    private String notes;             // 비고

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReservationDate() { return reservationDate; }
    public void setReservationDate(String reservationDate) { this.reservationDate = reservationDate; }

    public String getReservationTime() { return reservationTime; }
    public void setReservationTime(String reservationTime) { this.reservationTime = reservationTime; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Integer getPeopleCount() { return peopleCount; }
    public void setPeopleCount(Integer peopleCount) { this.peopleCount = peopleCount; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
