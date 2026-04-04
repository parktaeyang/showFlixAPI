package com.showflix.api.admin.domain;

/**
 * Domain Layer - 멤버십 회원 도메인 모델
 * 테이블: sf_membership
 */
public class Membership {

    private Long id;
    private String memberName;
    private String phone;
    private String joinDate;
    private String expireDate;
    private String memo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public String getExpireDate() { return expireDate; }
    public void setExpireDate(String expireDate) { this.expireDate = expireDate; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
}
