package com.showflix.api.schedule.interfaces;

import com.showflix.api.schedule.application.ScheduleSpecialService;
import com.showflix.api.schedule.domain.ScheduleSpecial;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 특수예약 관리 API
 * 경로: /api/admin/special/**
 */
@RestController
@RequestMapping("/api/admin/special")
@PreAuthorize("hasRole('ADMIN')")
public class ScheduleSpecialController {

    private final ScheduleSpecialService scheduleSpecialService;

    public ScheduleSpecialController(ScheduleSpecialService scheduleSpecialService) {
        this.scheduleSpecialService = scheduleSpecialService;
    }

    // DTO 정의 (record)
    record SpecialResponse(Long id, String reservationDate, String reservationTime,
                           String customerName, Integer peopleCount,
                           String contactInfo, String notes) {}

    record CreateSpecialRequest(String reservationDate, String reservationTime,
                                String customerName, Integer peopleCount,
                                String contactInfo, String notes) {}

    record UpdateSpecialRequest(String reservationDate, String reservationTime,
                                String customerName, Integer peopleCount,
                                String contactInfo, String notes) {}

    /**
     * 전체 특수예약 목록 조회
     * GET /api/admin/special
     */
    @GetMapping
    public ResponseEntity<List<SpecialResponse>> getAll() {
        List<ScheduleSpecial> specials = scheduleSpecialService.getAll();
        List<SpecialResponse> responses = specials.stream()
                .map(s -> new SpecialResponse(
                        s.getId(),
                        s.getReservationDate(),
                        s.getReservationTime(),
                        s.getCustomerName(),
                        s.getPeopleCount(),
                        s.getContactInfo(),
                        s.getNotes()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 특수예약 추가
     * POST /api/admin/special
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody CreateSpecialRequest request) {
        scheduleSpecialService.create(
                request.reservationDate(),
                request.reservationTime(),
                request.customerName(),
                request.peopleCount(),
                request.contactInfo(),
                request.notes()
        );
        return ResponseEntity.ok(Map.of("message", "특수예약이 추가되었습니다."));
    }

    /**
     * 특수예약 수정
     * PUT /api/admin/special/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(
            @PathVariable Long id,
            @RequestBody UpdateSpecialRequest request) {
        scheduleSpecialService.update(
                id,
                request.reservationDate(),
                request.reservationTime(),
                request.customerName(),
                request.peopleCount(),
                request.contactInfo(),
                request.notes()
        );
        return ResponseEntity.ok(Map.of("message", "특수예약이 수정되었습니다."));
    }

    /**
     * 특수예약 삭제
     * DELETE /api/admin/special/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        scheduleSpecialService.delete(id);
        return ResponseEntity.ok(Map.of("message", "특수예약이 삭제되었습니다."));
    }
}
