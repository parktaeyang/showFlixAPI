package com.showflix.api.admin.interfaces;

import com.showflix.api.admin.application.MonthlyNoteService;
import com.showflix.api.admin.domain.MonthlyNote;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Interfaces Layer - 월별 주요사항 API
 * 경로: /api/admin/monthly-note
 */
@RestController
@RequestMapping("/api/admin/monthly-note")
@PreAuthorize("hasRole('ADMIN')")
public class MonthlyNoteController {

    private final MonthlyNoteService service;

    public MonthlyNoteController(MonthlyNoteService service) {
        this.service = service;
    }

    record NoteResponse(int year, int month, String content) {}
    record NoteRequest(int year, int month, String content) {}

    /**
     * 월별 주요사항 조회
     * GET /api/admin/monthly-note?year=2026&month=3
     */
    @GetMapping
    public ResponseEntity<NoteResponse> get(@RequestParam int year, @RequestParam int month) {
        MonthlyNote note = service.getByYearMonth(year, month);
        String content = note != null ? note.getContent() : "";
        return ResponseEntity.ok(new NoteResponse(year, month, content));
    }

    /**
     * 월별 주요사항 저장/수정
     * PUT /api/admin/monthly-note
     * body: { year, month, content }
     */
    @PutMapping
    public ResponseEntity<Void> save(@RequestBody NoteRequest req, Authentication auth) {
        MonthlyNote note = new MonthlyNote();
        note.setYear(req.year());
        note.setMonth(req.month());
        note.setContent(req.content());
        note.setUpdatedBy(auth != null ? auth.getName() : "admin");
        service.save(note);
        return ResponseEntity.ok().build();
    }
}
