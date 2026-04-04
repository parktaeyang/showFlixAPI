package com.showflix.api.admin.interfaces;

import com.showflix.api.admin.application.DailyStatusService;
import com.showflix.api.admin.domain.DailyStatusEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 일일 현황 API
 * sf_selected_date + sf_schedule 조합
 * 경로: /api/admin/daily-status
 */
@RestController
@RequestMapping("/api/admin/daily-status")
@PreAuthorize("hasRole('ADMIN')")
public class DailyStatusController {

    private final DailyStatusService service;

    public DailyStatusController(DailyStatusService service) {
        this.service = service;
    }

    record DailyStatusResponse(String userId, String userName, String role,
                               String confirmed, boolean openHope, String remarks,
                               String accountType, Double hours, String scheduleMemo) {}

    /**
     * 일일 상세 현황 조회
     * GET /api/admin/daily-status?date=2026-04-04
     */
    @GetMapping
    public ResponseEntity<List<DailyStatusResponse>> getByDate(@RequestParam String date) {
        List<DailyStatusResponse> responses = service.getByDate(date).stream()
                .map(e -> new DailyStatusResponse(
                        e.getUserId(), e.getUserName(), e.getRole(),
                        e.getConfirmed(), e.isOpenHope(), e.getRemarks(),
                        e.getAccountType(), e.getHours(), e.getScheduleMemo()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
