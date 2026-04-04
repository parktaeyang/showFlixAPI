package com.showflix.api.admin.domain;

/**
 * Domain Layer - 크리스마스 지정석 도메인 모델
 * 테이블: sf_xmas_seat
 */
public class XmasSeat {

    private Long id;
    private String eventDate;
    private String seatLabel;
    private String customerName;
    private String phone;
    private Integer peopleCount;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getSeatLabel() { return seatLabel; }
    public void setSeatLabel(String seatLabel) { this.seatLabel = seatLabel; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getPeopleCount() { return peopleCount; }
    public void setPeopleCount(Integer peopleCount) { this.peopleCount = peopleCount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
