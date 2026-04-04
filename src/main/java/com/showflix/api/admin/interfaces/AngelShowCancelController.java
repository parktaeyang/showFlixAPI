package com.showflix.api.admin.interfaces;

import com.showflix.api.admin.application.AngelShowCancelService;
import com.showflix.api.admin.domain.AngelShowCancel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 엔젤쇼 취소현황 API
 * 경로: /api/admin/angel-cancel
 */
@RestController
@RequestMapping("/api/admin/angel-cancel")
@PreAuthorize("hasRole('ADMIN')")
public class AngelShowCancelController {

    private final AngelShowCancelService service;

    public AngelShowCancelController(AngelShowCancelService service) {
        this.service = service;
    }

    record AngelCancelResponse(Long id, String cancelDate, String showTime,
                               String reason, String actorName, String notes) {}

    record CreateAngelCancelRequest(String cancelDate, String showTime,
                                    String reason, String actorName, String notes) {}

    record UpdateAngelCancelRequest(String cancelDate, String showTime,
                                    String reason, String actorName, String notes) {}

    /**
     * 월별 엔젤쇼 취소 목록 조회
     * GET /api/admin/angel-cancel?year=2026&month=4
     */
    @GetMapping
    public ResponseEntity<List<AngelCancelResponse>> getByMonth(
            @RequestParam int year, @RequestParam int month) {
        List<AngelShowCancel> list = service.getByMonth(year, month);
        List<AngelCancelResponse> responses = list.stream()
                .map(e -> new AngelCancelResponse(
                        e.getId(), e.getCancelDate(), e.getShowTime(),
                        e.getReason(), e.getActorName(), e.getNotes()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** POST /api/admin/angel-cancel */
    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody CreateAngelCancelRequest request) {
        service.create(request.cancelDate(), request.showTime(),
                request.reason(), request.actorName(), request.notes());
        return ResponseEntity.ok(Map.of("message", "엔젤쇼 취소가 등록되었습니다."));
    }

    /** PUT /api/admin/angel-cancel/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(
            @PathVariable Long id, @RequestBody UpdateAngelCancelRequest request) {
        service.update(id, request.cancelDate(), request.showTime(),
                request.reason(), request.actorName(), request.notes());
        return ResponseEntity.ok(Map.of("message", "엔젤쇼 취소가 수정되었습니다."));
    }

    /** DELETE /api/admin/angel-cancel/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "엔젤쇼 취소가 삭제되었습니다."));
    }
}
