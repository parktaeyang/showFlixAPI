package com.schedulemng.controller.main;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/schedule/calendar")
    public String mainPage() {
        return "schedule/calendar";
    }
}
