package com.showflix.api.schedule.mapper;

import com.showflix.api.schedule.domain.WorkDiary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Infrastructure Layer - MyBatis Mapper (업무일지)
 */
@Mapper
public interface WorkDiaryMapper {

    List<WorkDiary> findByMonth(@Param("yearMonth") String yearMonth);

    WorkDiary findById(@Param("id") Long id);

    void insert(WorkDiary workDiary);

    void update(WorkDiary workDiary);

    void deleteById(@Param("id") Long id);
}
