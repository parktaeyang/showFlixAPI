package com.showflix.api.schedule.mapper;

import com.showflix.api.schedule.domain.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Infrastructure Layer - Schedule MyBatis Mapper
 */
@Mapper
public interface ScheduleMapper {

    List<Schedule> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    Schedule findByDateAndUsername(@Param("date") String date,
                                   @Param("username") String username);

    void insert(Schedule schedule);

    void update(Schedule schedule);

    int deleteByDateAndUsername(@Param("date") String date,
                                @Param("username") String username);

    int updateRemarksByDate(@Param("date") String date,
                            @Param("remarks") String remarks);
}
