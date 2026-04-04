package com.showflix.api.admin.domain;

/**
 * Domain Layer - 맥주 셀렉 도메인 모델
 * 테이블: sf_beer_select
 */
public class BeerSelect {

    private Long id;
    private String beerName;
    private String brand;
    private String category;
    private String notes;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBeerName() { return beerName; }
    public void setBeerName(String beerName) { this.beerName = beerName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
