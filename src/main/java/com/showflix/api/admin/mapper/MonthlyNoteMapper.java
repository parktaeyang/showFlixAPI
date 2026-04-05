package com.showflix.api.admin.mapper;

import com.showflix.api.admin.domain.MonthlyNote;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MonthlyNoteMapper {
    MonthlyNote findByYearMonth(@Param("year") int year, @Param("month") int month);
    void upsert(MonthlyNote note);
}
