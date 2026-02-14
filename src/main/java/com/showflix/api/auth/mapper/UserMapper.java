package com.showflix.api.auth.mapper;

import com.showflix.api.auth.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * Infrastructure Layer - MyBatis Mapper
 */
@Mapper
public interface UserMapper {

    User findByUserid(String userid);

}
