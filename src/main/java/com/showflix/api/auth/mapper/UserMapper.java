package com.showflix.api.auth.mapper;

import com.showflix.api.auth.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Infrastructure Layer - MyBatis Mapper
 */
@Mapper
public interface UserMapper {

    User findByUserid(String userid);

    List<User> findActors();

}
