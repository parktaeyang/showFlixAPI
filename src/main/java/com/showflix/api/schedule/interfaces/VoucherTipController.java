package com.showflix.api.schedule.interfaces;

import com.showflix.api.schedule.application.VoucherTipService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Interfaces Layer - 바우처/팁 관리 API
 * 경로: /api/admin/voucher/**
 */
@RestController
@RequestMapping("/api/admin/voucher")
@PreAuthorize("hasRole('ADMIN')")
public class VoucherTipController {

    private final VoucherTipService voucherTipService;

    public VoucherTipController(VoucherTipService voucherTipService) {
        this.voucherTipService = voucherTipService;
    }

    /**
     * @deprecated 일별 조회 — 월별 API(/monthly)로 대체
     * GET /api/admin/voucher?date=2025-03-08
     */
    @Deprecated
    @GetMapping
    public ResponseEntity<List<VoucherTipService.VoucherTipEntry>> getByDate(
            @RequestParam String date) {
        List<VoucherTipService.VoucherTipEntry> result = voucherTipService.getByDate(date);
        return ResponseEntity.ok(result);
    }

    /**
     * @deprecated 일별 저장 — 월별 API(/monthly/save)로 대체
     * POST /api/admin/voucher/save
     */
    @Deprecated
    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> save(@RequestBody SaveRequest request) {
        if (request.date() == null || request.date().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "날짜를 입력해주세요."));
        }
        voucherTipService.saveAll(request.date(), request.entries());
        return ResponseEntity.ok(Map.of("message", "저장되었습니다."));
    }

    /**
     * 월별 배우 바우처/팁 그리드 데이터 조회
     * GET /api/admin/voucher/monthly?year=2026&month=4
     */
    @GetMapping("/monthly")
    public ResponseEntity<VoucherTipService.MonthResult> getMonthData(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(voucherTipService.getMonthData(year, month));
    }

    /**
     * 월별 바우처/팁 엑셀 다운로드
     * GET /api/admin/voucher/monthly/export?year=2026&month=4
     */
    @GetMapping("/monthly/export")
    public ResponseEntity<byte[]> exportMonthly(
            @RequestParam int year, @RequestParam int month) {
        VoucherTipService.MonthResult result = voucherTipService.getMonthData(year, month);
        byte[] excelData = voucherTipService.exportToExcel(year, month, result);
        String filename = year + "년_" + month + "월_바우처팁.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    /**
     * 월별 바우처/팁 일괄 저장 (그리드 데이터 bulk upsert)
     * POST /api/admin/voucher/monthly/save
     */
    @PostMapping("/monthly/save")
    public ResponseEntity<Map<String, String>> saveMonthly(
            @RequestBody MonthSaveRequest request) {
        voucherTipService.saveBulk(request.entries());
        return ResponseEntity.ok(Map.of("message", "저장되었습니다."));
    }

    record SaveRequest(String date, List<VoucherTipService.SaveEntry> entries) {}
    record MonthSaveRequest(List<VoucherTipService.DailySaveEntry> entries) {}
}
