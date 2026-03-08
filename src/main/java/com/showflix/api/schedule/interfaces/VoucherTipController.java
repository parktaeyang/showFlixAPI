package com.showflix.api.schedule.interfaces;

import com.showflix.api.schedule.application.VoucherTipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
     * 특정 날짜 출근자 + 바우처/팁 조회
     * GET /api/admin/voucher?date=2025-03-08
     */
    @GetMapping
    public ResponseEntity<List<VoucherTipService.VoucherTipEntry>> getByDate(
            @RequestParam String date) {
        List<VoucherTipService.VoucherTipEntry> result = voucherTipService.getByDate(date);
        return ResponseEntity.ok(result);
    }

    /**
     * 바우처/팁 일괄 저장
     * POST /api/admin/voucher/save
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> save(@RequestBody SaveRequest request) {
        if (request.date() == null || request.date().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "날짜를 입력해주세요."));
        }
        voucherTipService.saveAll(request.date(), request.entries());
        return ResponseEntity.ok(Map.of("message", "저장되었습니다."));
    }

    record SaveRequest(String date, List<VoucherTipService.SaveEntry> entries) {}
}
