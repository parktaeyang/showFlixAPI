package com.showflix.api.auth.domain;

import java.util.Optional;

/**
 * Domain Layer - User 저장소 Port
 */
public interface UserRepository {

    Optional<User> findByUserid(String userid);

}

