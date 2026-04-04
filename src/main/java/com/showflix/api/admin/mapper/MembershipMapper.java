package com.showflix.api.admin.mapper;

import com.showflix.api.admin.domain.Membership;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MembershipMapper {

    List<Membership> findAll();

    Membership findById(@Param("id") Long id);

    void insert(Membership membership);

    void update(Membership membership);

    void deleteById(@Param("id") Long id);
}
