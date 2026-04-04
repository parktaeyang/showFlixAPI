package com.showflix.api.admin.mapper;

import com.showflix.api.admin.domain.Partner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PartnerMapper {

    List<Partner> findAll();

    Partner findById(@Param("id") Long id);

    void insert(Partner partner);

    void update(Partner partner);

    void deleteById(@Param("id") Long id);
}
