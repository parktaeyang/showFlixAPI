package com.showflix.api.admin.interfaces;

import com.showflix.api.admin.application.DailyNoteService;
import com.showflix.api.admin.domain.DailyNote;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 날짜별 메모 API
 * 경로: /api/admin/daily-note
 */
@RestController
@RequestMapping("/api/admin/daily-note")
@PreAuthorize("hasRole('ADMIN')")
public class DailyNoteController {

    private final DailyNoteService service;

    public DailyNoteController(DailyNoteService service) {
        this.service = service;
    }

    record NoteResponse(String noteDate, String content) {}
    record NoteRequest(String noteDate, String content) {}

    /**
     * 연간 날짜별 메모 조회
     * GET /api/admin/daily-note?year=2026
     */
    @GetMapping
    public ResponseEntity<List<NoteResponse>> getByYear(@RequestParam int year) {
        List<NoteResponse> responses = service.getByYear(year).stream()
                .map(n -> new NoteResponse(n.getNoteDate(), n.getContent()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 날짜별 메모 저장/수정
     * PUT /api/admin/daily-note
     * body: { noteDate: "YYYY-MM-DD", content: "..." }
     */
    @PutMapping
    public ResponseEntity<Void> save(@RequestBody NoteRequest req, Authentication auth) {
        if (req.noteDate() == null || !req.noteDate().matches("\\d{4}-\\d{2}-\\d{2}")) {
            return ResponseEntity.badRequest().build();
        }
        DailyNote note = new DailyNote();
        note.setNoteDate(req.noteDate());
        note.setContent(req.content());
        note.setUpdatedBy(auth != null ? auth.getName() : "admin");
        service.save(note);
        return ResponseEntity.ok().build();
    }
}
