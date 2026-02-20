package com.showflix.api.schedule.mapper;

import com.showflix.api.schedule.domain.ScheduleSpecial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Infrastructure Layer - ScheduleSpecial MyBatis Mapper
 */
@Mapper
public interface ScheduleSpecialMapper {

    List<ScheduleSpecial> findAll();

    ScheduleSpecial findById(@Param("id") Long id);

    void insert(ScheduleSpecial scheduleSpecial);

    void update(ScheduleSpecial scheduleSpecial);

    void deleteById(@Param("id") Long id);
}
