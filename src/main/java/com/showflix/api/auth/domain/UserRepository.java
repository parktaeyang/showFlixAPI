package com.showflix.api.auth.domain;

import java.util.List;
import java.util.Optional;

/**
 * Domain Layer - User 저장소 Port
 */
public interface UserRepository {

    Optional<User> findByUserid(String userid);

    // 배우(비관리자) 목록 조회 - Schedule 도메인에서 스케줄 테이블 열 구성에 사용
    List<User> findActors();

    // ── 관리자 계정관리용 ──────────────────────────────────────
    List<User> findAll();

    // 정렬 조건을 적용한 전체 사용자 목록 조회
    // sortBy: "userid" | "username", sortDir: "asc" | "desc"
    List<User> findAllSorted(String sortBy, String sortDir);

    // 계정유형별 마지막 userid 조회 (다음 번호 자동생성용)
    Optional<String> findLastUseridByAccountType(String accountType);

    void save(User user);

    void update(User user);

    void updatePassword(String userid, String encodedPassword);

    void delete(String userid);

}

