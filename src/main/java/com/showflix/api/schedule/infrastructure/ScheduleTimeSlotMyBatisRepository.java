package com.showflix.api.schedule.infrastructure;

import com.showflix.api.schedule.domain.ScheduleTimeSlot;
import com.showflix.api.schedule.domain.ScheduleTimeSlotRepository;
import com.showflix.api.schedule.mapper.ScheduleTimeSlotMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ScheduleTimeSlotMyBatisRepository implements ScheduleTimeSlotRepository {

    private final ScheduleTimeSlotMapper mapper;

    public ScheduleTimeSlotMyBatisRepository(ScheduleTimeSlotMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ScheduleTimeSlot> findByScheduleDate(String scheduleDate) {
        return mapper.findByScheduleDate(scheduleDate);
    }

    @Override
    public void save(ScheduleTimeSlot slot) {
        mapper.save(slot);
    }

    @Override
    public void deleteByScheduleDate(String scheduleDate) {
        mapper.deleteByScheduleDate(scheduleDate);
    }

    @Override
    public void updateConfirmationByDate(String scheduleDate, String confirmed) {
        mapper.updateConfirmationByDate(scheduleDate, confirmed);
    }
}
