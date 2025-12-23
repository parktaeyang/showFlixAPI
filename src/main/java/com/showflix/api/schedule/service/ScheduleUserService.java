package com.showflix.api.schedule.service;

import com.showflix.api.schedule.mapper.ScheduleUserMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ScheduleUserService {

    private final ScheduleUserMapper scheduleUserMapper;

    public ScheduleUserService(ScheduleUserMapper scheduleUserMapper) {
        this.scheduleUserMapper = scheduleUserMapper;
    }

    public Map<String, Object> getFirstUser() {
        return scheduleUserMapper.selectFirstUser();
    }

}


