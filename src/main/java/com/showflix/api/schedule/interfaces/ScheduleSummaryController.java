package com.showflix.api.schedule.interfaces;

import com.showflix.api.schedule.application.ScheduleSummaryExcelService;
import com.showflix.api.schedule.application.ScheduleSummaryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Interfaces Layer - 출근시간 요약 API 엔드포인트 (관리자 전용)
 */
@RestController
@RequestMapping("/api/admin/schedule-summary")
@PreAuthorize("hasRole('ADMIN')")
public class ScheduleSummaryController {

    private final ScheduleSummaryService service;
    private final ScheduleSummaryExcelService excelService;

    public ScheduleSummaryController(ScheduleSummaryService service,
                                     ScheduleSummaryExcelService excelService) {
        this.service = service;
        this.excelService = excelService;
    }

    /**
     * 월별 출근시간 데이터 조회
     * GET /api/admin/schedule-summary/month?year=2026&month=3
     */
    @GetMapping("/month")
    public ResponseEntity<ScheduleSummaryService.MonthResult> getMonthData(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(service.getMonthData(year, month));
    }

    /**
     * 출근시간 일괄 저장 (변경된 셀만)
     * POST /api/admin/schedule-summary/save
     * Body: [{ "userId": "A0001", "date": "2026-03-01", "hours": "8", "remarks": null }, ...]
     */
    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody List<SaveRequest> requests) {
        List<ScheduleSummaryService.SaveItem> items = requests.stream()
                .map(r -> new ScheduleSummaryService.SaveItem(r.userId(), r.date(), r.hours(), r.remarks()))
                .toList();
        service.saveBulk(items);
        return ResponseEntity.ok().build();
    }

    /**
     * 월별 출근시간 Excel 다운로드
     * GET /api/admin/schedule-summary/export?year=2026&month=3
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam int year,
            @RequestParam int month) {
        ScheduleSummaryService.MonthResult result = service.getMonthData(year, month);
        byte[] excelBytes = excelService.generate(year, month, result);

        String filename = year + "년_" + month + "월_출근시간.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    // ── Request DTO ──────────────────────────────────────────────────────

    record SaveRequest(String userId, String date, String hours, String remarks) {}
}
