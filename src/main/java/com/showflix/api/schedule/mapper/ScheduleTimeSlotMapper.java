package com.showflix.api.schedule.mapper;

import com.showflix.api.schedule.domain.ScheduleTimeSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScheduleTimeSlotMapper {

    List<ScheduleTimeSlot> findByScheduleDate(@Param("scheduleDate") String scheduleDate);

    void save(ScheduleTimeSlot slot);

    void deleteByScheduleDate(@Param("scheduleDate") String scheduleDate);

    void updateConfirmationByDate(@Param("scheduleDate") String scheduleDate,
                                  @Param("confirmed") String confirmed);
}
