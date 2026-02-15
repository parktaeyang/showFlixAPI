package com.showflix.api.schedule.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 캘린더 페이지 컨트롤러
 * RESTful 경로로 정적 HTML 리소스를 서빙
 */
@RestController
public class CalendarController {

    /**
     * /schedule/calendar 경로로 접근 시 정적 HTML 파일 반환
     * .html 확장자 없이 깔끔한 RESTful 경로 사용
     */
    @GetMapping("/schedule/calendar")
    public ResponseEntity<Resource> calendar() throws IOException {
        Resource resource = new ClassPathResource("static/schedule/calendar.html");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .body(resource);
    }
}
