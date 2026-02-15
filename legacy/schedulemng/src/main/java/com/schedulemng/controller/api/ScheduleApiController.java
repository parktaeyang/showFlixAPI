package com.schedulemng.controller.api;

import com.schedulemng.entity.Schedule;
import com.schedulemng.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleApiController {

    private final ScheduleRepository scheduleRepository;

    @GetMapping
    public List<Schedule> getSchedules() {
        return scheduleRepository.findAll();
    }

    @PostMapping
    public Schedule create(@RequestBody Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
}
