package com.showflix.api.schedule.infrastructure;

import com.showflix.api.schedule.domain.ScheduleSpecial;
import com.showflix.api.schedule.domain.ScheduleSpecialRepository;
import com.showflix.api.schedule.mapper.ScheduleSpecialMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Infrastructure Layer - ScheduleSpecialRepository 구현체 (MyBatis)
 */
@Repository
public class ScheduleSpecialMyBatisRepository implements ScheduleSpecialRepository {

    private final ScheduleSpecialMapper scheduleSpecialMapper;

    public ScheduleSpecialMyBatisRepository(ScheduleSpecialMapper scheduleSpecialMapper) {
        this.scheduleSpecialMapper = scheduleSpecialMapper;
    }

    @Override
    public List<ScheduleSpecial> findAll() {
        return scheduleSpecialMapper.findAll();
    }

    @Override
    public Optional<ScheduleSpecial> findById(Long id) {
        return Optional.ofNullable(scheduleSpecialMapper.findById(id));
    }

    @Override
    public void insert(ScheduleSpecial scheduleSpecial) {
        scheduleSpecialMapper.insert(scheduleSpecial);
    }

    @Override
    public void update(ScheduleSpecial scheduleSpecial) {
        scheduleSpecialMapper.update(scheduleSpecial);
    }

    @Override
    public void deleteById(Long id) {
        scheduleSpecialMapper.deleteById(id);
    }
}
