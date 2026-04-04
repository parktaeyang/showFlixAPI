package com.showflix.api.admin.domain;

/**
 * Domain Layer - 협력업체 도메인 모델
 * 테이블: sf_partner
 */
public class Partner {

    private Long id;
    private String category;
    private String name;
    private String contact;
    private String manager;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
