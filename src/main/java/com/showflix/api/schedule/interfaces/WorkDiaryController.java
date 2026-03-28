package com.showflix.api.schedule.interfaces;

import com.showflix.api.schedule.application.WorkDiaryExcelService;
import com.showflix.api.schedule.application.WorkDiaryService;
import com.showflix.api.schedule.domain.WorkDiary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Interfaces Layer - 업무일지 API 엔드포인트 (관리자 전용)
 */
@RestController
@RequestMapping("/api/admin/work-diary")
@PreAuthorize("hasRole('ADMIN')")
public class WorkDiaryController {

    private final WorkDiaryService service;
    private final WorkDiaryExcelService excelService;

    public WorkDiaryController(WorkDiaryService service, WorkDiaryExcelService excelService) {
        this.service = service;
        this.excelService = excelService;
    }

    /**
     * 월별 업무일지 조회
     * GET /api/admin/work-diary?year=2026&month=3
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        List<WorkDiary> list = service.getByMonth(year, month);
        return ResponseEntity.ok(Map.of("success", true, "data", list));
    }

    /**
     * 업무일지 신규 저장
     * POST /api/admin/work-diary
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody WorkDiaryRequest request) {
        WorkDiary created = service.create(toItem(request));
        return ResponseEntity.ok(Map.of("success", true, "data", created));
    }

    /**
     * 업무일지 수정
     * PUT /api/admin/work-diary/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody WorkDiaryRequest request) {
        WorkDiary updated = service.update(id, toItem(request));
        return ResponseEntity.ok(Map.of("success", true, "data", updated));
    }

    /**
     * 업무일지 삭제
     * DELETE /api/admin/work-diary/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "업무일지가 삭제되었습니다."));
    }

    /**
     * 월별 업무일지 Excel 다운로드
     * GET /api/admin/work-diary/export?year=2026&month=3
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam int year,
            @RequestParam int month) {
        List<WorkDiary> list = service.getByMonth(year, month);
        byte[] excelBytes = excelService.generate(year, month, list);

        String filename = year + "년_" + month + "월_업무일지.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    // ── Request DTO ────────────────────────────────────────────────────

    record WorkDiaryRequest(
            String date,
            String manager,
            String cashPayment,
            String reservations,
            String event,
            String storeRelated,
            String notes
    ) {}

    private WorkDiaryService.WorkDiaryItem toItem(WorkDiaryRequest r) {
        return new WorkDiaryService.WorkDiaryItem(
                r.date(), r.manager(), r.cashPayment(), r.reservations(),
                r.event(), r.storeRelated(), r.notes()
        );
    }
}
