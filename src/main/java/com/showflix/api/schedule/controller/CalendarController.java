package com.showflix.api.schedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 캘린더 페이지 컨트롤러
 */
@Controller
public class CalendarController {

    /**
     * /schedule/calendar 경로로 접근 시 calendar.html로 리다이렉트
     */
    @GetMapping("/schedule/calendar")
    public String calendar() {
        return "redirect:/schedule/calendar.html";
    }
}
