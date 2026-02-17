package com.showflix.api.schedule.infrastructure;

import com.showflix.api.schedule.domain.Schedule;
import com.showflix.api.schedule.domain.ScheduleRepository;
import com.showflix.api.schedule.mapper.ScheduleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Infrastructure Layer - ScheduleRepository 구현체 (MyBatis)
 */
@Repository
public class ScheduleMyBatisRepository implements ScheduleRepository {

    private final ScheduleMapper scheduleMapper;

    public ScheduleMyBatisRepository(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    public List<Schedule> findByYearAndMonth(int year, int month) {
        return scheduleMapper.findByYearAndMonth(year, month);
    }

    @Override
    public Optional<Schedule> findByDateAndUsername(String date, String username) {
        return Optional.ofNullable(scheduleMapper.findByDateAndUsername(date, username));
    }

    @Override
    public void insert(Schedule schedule) {
        scheduleMapper.insert(schedule);
    }

    @Override
    public void update(Schedule schedule) {
        scheduleMapper.update(schedule);
    }

    @Override
    public int deleteByDateAndUsername(String date, String username) {
        return scheduleMapper.deleteByDateAndUsername(date, username);
    }

    @Override
    public int updateRemarksByDate(String date, String remarks) {
        return scheduleMapper.updateRemarksByDate(date, remarks);
    }
}
