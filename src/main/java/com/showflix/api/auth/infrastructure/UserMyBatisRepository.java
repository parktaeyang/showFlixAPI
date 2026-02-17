package com.showflix.api.auth.infrastructure;

import com.showflix.api.auth.domain.User;
import com.showflix.api.auth.domain.UserRepository;
import com.showflix.api.auth.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Infrastructure Layer - UserRepository 구현체 (MyBatis)
 */
@Repository
public class UserMyBatisRepository implements UserRepository {

    private final UserMapper userMapper;

    public UserMyBatisRepository(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findByUserid(String userid) {
        return Optional.ofNullable(userMapper.findByUserid(userid));
    }

    @Override
    public List<User> findActors() {
        return userMapper.findActors();
    }
}

