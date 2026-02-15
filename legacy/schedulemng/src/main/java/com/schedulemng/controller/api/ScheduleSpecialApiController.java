package com.schedulemng.controller.api;

import com.schedulemng.dto.ScheduleSpecialRequestDto;
import com.schedulemng.dto.ScheduleSpecialResponseDto;
import com.schedulemng.service.ScheduleSpecialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule-special")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ScheduleSpecialApiController {
    
    private final ScheduleSpecialService scheduleSpecialService;
    
    /**
     * 모든 특수 예약 조회
     */
    @GetMapping
    public ResponseEntity<List<ScheduleSpecialResponseDto>> getAllSpecialReservations() {
        try {
            List<ScheduleSpecialResponseDto> reservations = scheduleSpecialService.getAllSpecialReservations();
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error fetching all special reservations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 페이징된 특수 예약 조회
     */
    @GetMapping("/paged")
    public ResponseEntity<ScheduleSpecialResponseDto.PagedResponse> getSpecialReservationsWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            ScheduleSpecialResponseDto.PagedResponse response = 
                scheduleSpecialService.getSpecialReservationsWithPaging(page, size, sortBy, sortDir);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching paged special reservations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * ID로 특수 예약 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleSpecialResponseDto> getSpecialReservationById(@PathVariable Long id) {
        try {
            return scheduleSpecialService.getSpecialReservationById(id)
                    .map(reservation -> ResponseEntity.ok(reservation))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching special reservation with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 특수 예약 생성
     */
    @PostMapping
    public ResponseEntity<?> createSpecialReservation(
            @Valid @RequestBody ScheduleSpecialRequestDto requestDto) {
        try {
            log.info("Creating new special reservation: {}", requestDto);
            ScheduleSpecialResponseDto response = scheduleSpecialService.createSpecialReservation(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating special reservation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create reservation", "message", e.getMessage()));
        }
    }
    
    /**
     * 특수 예약 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleSpecialResponseDto> updateSpecialReservation(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleSpecialRequestDto requestDto) {
        try {
            log.info("Updating special reservation with ID: {}", id);
            return scheduleSpecialService.updateSpecialReservation(id, requestDto)
                    .map(reservation -> ResponseEntity.ok(reservation))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error updating special reservation with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 특수 예약 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSpecialReservation(@PathVariable Long id) {
        try {
            log.info("Deleting special reservation with ID: {}", id);
            boolean deleted = scheduleSpecialService.deleteSpecialReservation(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Special reservation deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting special reservation with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete special reservation"));
        }
    }
    
    /**
     * 예약 상태별 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ScheduleSpecialResponseDto>> getSpecialReservationsByStatus(
            @PathVariable String status) {
        try {
            List<ScheduleSpecialResponseDto> reservations = 
                scheduleSpecialService.getSpecialReservationsByStatus(status);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error fetching special reservations by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 검색 기능
     */
    @GetMapping("/search")
    public ResponseEntity<List<ScheduleSpecialResponseDto>> searchSpecialReservations(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String contactInfo,
            @RequestParam(required = false) String specialRemarks,
            @RequestParam(required = false) String status) {
        try {
            List<ScheduleSpecialResponseDto> reservations = scheduleSpecialService.searchSpecialReservations(
                    customerName, contactInfo, specialRemarks, status);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error searching special reservations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 통계 정보 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<ScheduleSpecialResponseDto.StatisticsResponse> getStatistics() {
        try {
            ScheduleSpecialResponseDto.StatisticsResponse statistics = scheduleSpecialService.getStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 하이라이트 타입 변경
     */
    @PatchMapping("/{id}/highlight")
    public ResponseEntity<ScheduleSpecialResponseDto> updateHighlightType(
            @PathVariable Long id,
            @RequestParam String highlightType) {
        try {
            return scheduleSpecialService.updateHighlightType(id, highlightType)
                    .map(reservation -> ResponseEntity.ok(reservation))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error updating highlight type for reservation ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 예약 상태 변경
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ScheduleSpecialResponseDto> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            return scheduleSpecialService.updateReservationStatus(id, status)
                    .map(reservation -> ResponseEntity.ok(reservation))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error updating reservation status for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 일괄 업데이트 (여러 예약의 상태를 한번에 변경)
     */
    @PatchMapping("/batch-update")
    public ResponseEntity<Map<String, String>> batchUpdateReservations(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) request.get("ids");
            String status = (String) request.get("status");
            String highlightType = (String) request.get("highlightType");
            
            int updatedCount = 0;
            for (Long id : ids) {
                if (status != null) {
                    scheduleSpecialService.updateReservationStatus(id, status);
                    updatedCount++;
                }
                if (highlightType != null) {
                    scheduleSpecialService.updateHighlightType(id, highlightType);
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Batch update completed",
                "updatedCount", String.valueOf(updatedCount)
            ));
        } catch (Exception e) {
            log.error("Error in batch update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to perform batch update"));
        }
    }
}
