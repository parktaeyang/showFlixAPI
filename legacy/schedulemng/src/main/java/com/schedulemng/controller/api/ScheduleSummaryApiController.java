package com.schedulemng.controller.api;

import com.schedulemng.dto.ScheduleDto;
import com.schedulemng.dto.DailyRemarksDto;
import com.schedulemng.dto.BatchResult;
import com.schedulemng.dto.ScheduleTableDTO;
import com.schedulemng.service.ScheduleSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule-summary")
@RequiredArgsConstructor
@Slf4j
public class ScheduleSummaryApiController {
    
    private final ScheduleSummaryService scheduleSummaryService;
    
    /**
     * 스케줄 테이블 데이터 조회
     */
    @GetMapping("/schedule-table")
    public ResponseEntity<Map<String, Object>> getScheduleTable(
            @RequestParam int year,
            @RequestParam int month) {
        try {
            log.info("스케줄 테이블 조회 요청: {}년 {}월", year, month);
            
            ScheduleTableDTO scheduleTable = scheduleSummaryService.getScheduleTable(year, month);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", scheduleTable
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("스케줄 테이블 조회 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "스케줄 테이블 조회 중 오류가 발생했습니다."
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 개별 스케줄 저장
     */
    @PostMapping("/save-schedule")
    public ResponseEntity<Map<String, Object>> saveSchedule(@RequestBody ScheduleDto scheduleDto) {
        try {
            log.info("개별 스케줄 저장 요청: {}", scheduleDto);
            
            boolean success = scheduleSummaryService.saveSchedule(
                scheduleDto.getDate(),
                scheduleDto.getUsername(),
                scheduleDto.getHours()
            );
            
            Map<String, Object> response = Map.of(
                "success", success,
                "message", success ? "스케줄이 저장되었습니다." : "스케줄 저장에 실패했습니다."
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("개별 스케줄 저장 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "스케줄 저장 중 오류가 발생했습니다."
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 일별 특이사항 저장
     */
    @PostMapping("/update-daily-remarks")
    public ResponseEntity<Map<String, Object>> updateDailyRemarks(@RequestBody DailyRemarksDto remarksDto) {
        try {
            log.info("일별 특이사항 저장 요청: {}", remarksDto);
            
            boolean success = scheduleSummaryService.saveDailyRemarks(
                remarksDto.getDate(),
                remarksDto.getRemarks()
            );
            
            Map<String, Object> response = Map.of(
                "success", success,
                "message", success ? "특이사항이 저장되었습니다." : "특이사항 저장에 실패했습니다."
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("일별 특이사항 저장 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "특이사항 저장 중 오류가 발생했습니다."
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 전체 스케줄 일괄 저장
     */
    @PostMapping("/save-all-schedules")
    public ResponseEntity<Map<String, Object>> saveAllSchedules(@RequestBody List<ScheduleDto> schedules) {
        try {
            log.info("전체 스케줄 일괄 저장 요청: {}개", schedules.size());
            
            BatchResult result = scheduleSummaryService.saveAllSchedules(schedules);
            
            Map<String, Object> response = Map.of(
                "success", result.isSuccess(),
                "totalCount", result.getTotalCount(),
                "successCount", result.getSuccessCount(),
                "failureCount", result.getFailureCount(),
                "message", String.format("총 %d개 중 %d개가 저장되었습니다.", 
                    result.getTotalCount(), result.getSuccessCount())
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("전체 스케줄 일괄 저장 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "전체 스케줄 저장 중 오류가 발생했습니다."
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 특정 사용자의 월별 출근 통계 조회
     */
    @GetMapping("/user-monthly-stats")
    public ResponseEntity<Map<String, Object>> getUserMonthlyStats(
            @RequestParam String userId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            log.info("사용자 월별 출근 통계 조회 요청: {} - {}년 {}월", userId, year, month);
            
            Map<String, Object> stats = scheduleSummaryService.getUserMonthlyStats(userId, year, month);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", stats
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("사용자 월별 출근 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "출근 통계 조회 중 오류가 발생했습니다."
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
} 