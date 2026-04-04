package com.showflix.api.admin.interfaces;

import com.showflix.api.admin.application.XmasSeatService;
import com.showflix.api.admin.domain.XmasSeat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 크리스마스 지정석 API
 * 경로: /api/admin/xmas-seats
 */
@RestController
@RequestMapping("/api/admin/xmas-seats")
@PreAuthorize("hasRole('ADMIN')")
public class XmasSeatController {

    private final XmasSeatService service;

    public XmasSeatController(XmasSeatService service) {
        this.service = service;
    }

    record XmasSeatResponse(Long id, String eventDate, String seatLabel,
                            String customerName, String phone,
                            Integer peopleCount, String notes) {}

    record CreateXmasSeatRequest(String eventDate, String seatLabel,
                                 String customerName, String phone,
                                 Integer peopleCount, String notes) {}

    record UpdateXmasSeatRequest(String eventDate, String seatLabel,
                                 String customerName, String phone,
                                 Integer peopleCount, String notes) {}

    /** GET /api/admin/xmas-seats?date=2026-12-25 */
    @GetMapping
    public ResponseEntity<List<XmasSeatResponse>> getByDate(@RequestParam String date) {
        List<XmasSeatResponse> responses = service.getByDate(date).stream()
                .map(e -> new XmasSeatResponse(
                        e.getId(), e.getEventDate(), e.getSeatLabel(),
                        e.getCustomerName(), e.getPhone(),
                        e.getPeopleCount(), e.getNotes()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** POST /api/admin/xmas-seats */
    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody CreateXmasSeatRequest request) {
        service.create(request.eventDate(), request.seatLabel(),
                request.customerName(), request.phone(),
                request.peopleCount(), request.notes());
        return ResponseEntity.ok(Map.of("message", "크리스마스 지정석이 등록되었습니다."));
    }

    /** PUT /api/admin/xmas-seats/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(
            @PathVariable Long id, @RequestBody UpdateXmasSeatRequest request) {
        service.update(id, request.eventDate(), request.seatLabel(),
                request.customerName(), request.phone(),
                request.peopleCount(), request.notes());
        return ResponseEntity.ok(Map.of("message", "크리스마스 지정석이 수정되었습니다."));
    }

    /** DELETE /api/admin/xmas-seats/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "크리스마스 지정석이 삭제되었습니다."));
    }
}
