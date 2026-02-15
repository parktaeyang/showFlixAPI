package com.schedulemng.repository;

import com.schedulemng.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUserid(String userid);

    // 계정유형별 최신 아이디 조회
    @Query("SELECT u.userid FROM User u WHERE u.accountType = :accountType ORDER BY u.userid DESC LIMIT 1")
    String findTopByAccountTypeOrderByUseridDesc(@Param("accountType") User.AccountType accountType);
    
    // 계정유형별 사용자 조회
    List<User> findByAccountType(User.AccountType accountType);
}