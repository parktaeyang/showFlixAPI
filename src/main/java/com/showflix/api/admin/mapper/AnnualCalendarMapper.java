package com.showflix.api.admin.mapper;

import com.showflix.api.admin.domain.AnnualCalendarEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnualCalendarMapper {

    List<AnnualCalendarEntry> findByYear(@Param("year") int year);
}
