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

    /**
     * /admin/ 경로로 접근 시 관리자 페이지 HTML 파일 반환
     * SecurityConfig에서 ADMIN 권한 체크 후 이 메서드가 실행됨
     */
    @GetMapping("/admin/")
    public ResponseEntity<Resource> adminPage() throws IOException {
        Resource resource = new ClassPathResource("static/admin/index.html");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .body(resource);
    }

    /**
     * /admin/schedule-summary → /admin/#staff 리다이렉트
     * 관리자 SPA 통합에 따라 기존 별도 페이지를 탭으로 리다이렉트
     */
    @GetMapping("/admin/schedule-summary")
    public ResponseEntity<Void> scheduleSummaryPage() {
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/admin/#staff")
                .build();
    }

    /**
     * /admin/work-diary → /admin/#work-diary 리다이렉트
     * 관리자 SPA 통합에 따라 기존 별도 페이지를 탭으로 리다이렉트
     */
    @GetMapping("/admin/work-diary")
    public ResponseEntity<Void> workDiaryPage() {
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/admin/#work-diary")
                .build();
    }
}
