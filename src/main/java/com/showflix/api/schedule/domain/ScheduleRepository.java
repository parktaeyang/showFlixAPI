package com.showflix.api.schedule.domain;

import java.util.List;
import java.util.Optional;

/**
 * Domain Layer - Schedule 저장소 Port
 */
public interface ScheduleRepository {

    List<Schedule> findByYearAndMonth(int year, int month);

    Optional<Schedule> findByDateAndUsername(String date, String username);

    void insert(Schedule schedule);

    void update(Schedule schedule);

    int deleteByDateAndUsername(String date, String username);

    int updateRemarksByDate(String date, String remarks);
}
