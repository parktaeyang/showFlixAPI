package com.showflix.api.schedule.controller;

import com.showflix.api.schedule.service.ScheduleUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/schedule-users")
public class ScheduleUserController {

    private final ScheduleUserService scheduleUserService;

    public ScheduleUserController(ScheduleUserService scheduleUserService) {
        this.scheduleUserService = scheduleUserService;
    }

    @GetMapping("/first")
    public Map<String, Object> getFirstUser() {
        System.out.println("getFirstUser");
        return scheduleUserService.getFirstUser();
    }

}