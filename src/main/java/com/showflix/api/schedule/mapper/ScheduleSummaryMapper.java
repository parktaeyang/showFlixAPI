package com.showflix.api.schedule.mapper;

import com.showflix.api.schedule.domain.ScheduleSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Infrastructure Layer - MyBatis Mapper (출근시간 요약)
 */
@Mapper
public interface ScheduleSummaryMapper {

    List<ScheduleSummary> findByMonth(@Param("start") String start, @Param("end") String end);

    void upsert(ScheduleSummary summary);
}
