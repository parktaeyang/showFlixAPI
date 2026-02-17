package com.showflix.api.schedule.mapper;

import com.showflix.api.schedule.domain.SelectedDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Infrastructure Layer - MyBatis Mapper
 */
@Mapper
public interface SelectedDateMapper {

    List<SelectedDate> findByDateBetween(@Param("start") String start, @Param("end") String end);

    void insert(SelectedDate selectedDate);

    int updateRoleByDateAndUserId(@Param("date") String date, @Param("userId") String userId,
                                  @Param("role") String role, @Param("remarks") String remarks);

    int deleteByDateAndUserId(@Param("date") String date, @Param("userId") String userId);

    int updateConfirmationByDate(@Param("date") String date, @Param("confirmed") String confirmed);
}
