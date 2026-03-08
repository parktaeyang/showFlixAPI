package com.showflix.api.auth.mapper;

import com.showflix.api.auth.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Infrastructure Layer - MyBatis Mapper
 */
@Mapper
public interface UserMapper {

    User findByUserid(String userid);

    List<User> findActors();

    // ── 관리자 계정관리용 ──────────────────────────────────────
    List<User> findAll();

    // 계정유형별 마지막 userid 조회 (다음 번호 자동생성용)
    String findLastUseridByAccountType(@Param("accountType") String accountType);

    void save(User user);

    void update(User user);

    void updatePassword(@Param("userid") String userid,
                        @Param("password") String encodedPassword);

    void delete(@Param("userid") String userid);

}
