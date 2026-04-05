package com.showflix.api.admin.mapper;

import com.showflix.api.admin.domain.DailyNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DailyNoteMapper {
    List<DailyNote> findByYear(@Param("year") int year);
    void upsert(DailyNote note);
}
