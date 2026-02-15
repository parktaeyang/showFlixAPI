package com.schedulemng.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "schedule_users")
@ToString
public class User implements UserDetails {

    @Id
    @Column(nullable = false, unique = true)
    private String userid;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column
    private Role role;

    @Column(nullable = false)
    private boolean isAdmin;

    @Column(nullable = false)
    private String createdAt;

    // 계정유형 enum
    @Getter
    public enum AccountType {
        ACTOR("배우"),
        STAFF("스텝"),
        CAPTAIN("캡틴"),
        ADMIN("관리자");

        private final String displayName;

        AccountType(String displayName) {
            this.displayName = displayName;
        }
    }

    // 역할 enum
    @Getter
    public enum Role {
        // 스텝 역할
        DOOR("도어"),
        HOLEMAN("홀맨"),
        OPER("오퍼"),
        HELPER("헬퍼"),
        KITCHEN("주방"),
        
        // 배우 역할
        MALE1("남1"),
        MALE2("남2"),
        MALE3("남3"),
        FEMALE1("여1"),
        FEMALE2("여2"),
        FEMALE3("여3");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(isAdmin ? "ROLE_ADMIN" : "ROLE_USER"));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
