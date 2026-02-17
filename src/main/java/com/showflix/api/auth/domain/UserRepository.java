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

}

