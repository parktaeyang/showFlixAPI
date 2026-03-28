package com.showflix.api.schedule.domain;

/**
 * Domain Layer - 업무일지 엔티티
 * work_diary 테이블 매핑
 */
public class WorkDiary {

    private Long id;
    private String date;          // YYYY-MM-DD
    private String manager;       // 담당자
    private String cashPayment;   // 현금 결제
    private String reservations;  // 지정석/특수예약/멤버십
    private String event;         // 이벤트
    private String storeRelated;  // 가게 관련
    private String notes;         // 특이사항

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public String getCashPayment() { return cashPayment; }
    public void setCashPayment(String cashPayment) { this.cashPayment = cashPayment; }

    public String getReservations() { return reservations; }
    public void setReservations(String reservations) { this.reservations = reservations; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getStoreRelated() { return storeRelated; }
    public void setStoreRelated(String storeRelated) { this.storeRelated = storeRelated; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
