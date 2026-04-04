package com.showflix.api.admin.mapper;

import com.showflix.api.admin.domain.AngelShowCancel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Infrastructure Layer - AngelShowCancel MyBatis Mapper
 */
@Mapper
public interface AngelShowCancelMapper {

    List<AngelShowCancel> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    AngelShowCancel findById(@Param("id") Long id);

    void insert(AngelShowCancel angelShowCancel);

    void update(AngelShowCancel angelShowCancel);

    void deleteById(@Param("id") Long id);
}
