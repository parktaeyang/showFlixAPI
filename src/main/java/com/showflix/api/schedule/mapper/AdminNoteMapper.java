package com.showflix.api.schedule.mapper;

import com.showflix.api.schedule.domain.AdminNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Infrastructure Layer - AdminNote MyBatis Mapper
 */
@Mapper
public interface AdminNoteMapper {

    AdminNote findById(@Param("id") String id);

    void upsert(AdminNote adminNote);
}
