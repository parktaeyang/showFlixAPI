package com.schedulemng.controller.api;

import com.schedulemng.dto.WorkLogDTO;
import com.schedulemng.service.WorkLogService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/work-logs")
@RequiredArgsConstructor
@Slf4j
public class WorkLogApiController {

    private final WorkLogService workLogService;

    /**
     * 업무일지 데이터 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getWorkLog(@RequestParam int year, @RequestParam int month) {
            log.info("업무일지 조회 요청: {}년 {}월", year, month);
            List<WorkLogDTO> workLogs = workLogService.getWorkLog(year, month);
            return ResponseEntity.ok(Map.of("success", true, "data", workLogs));
    }
    
    /**
     * 신규 업무일지 저장
     */
    @PostMapping("/save-workLog")
    public ResponseEntity<Map<String, Object>> saveSchedule(@RequestBody WorkLogDTO workLogDTO) {
        WorkLogDTO createdLog = workLogService.saveWorkLog(workLogDTO);
        return ResponseEntity.ok(Map.of("success", true, "data", createdLog));
    }

    /**
     * 업무일지 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateWorkLog(@PathVariable Long id, @RequestBody WorkLogDTO workLogDTO) {
        WorkLogDTO updateLog = workLogService.updateWorkLog(id, workLogDTO);
        return ResponseEntity.ok(Map.of("success", true, "data", updateLog));
    }

    /**
     * 업무일지 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteWorkLog(@PathVariable Long id) {
        workLogService.deleteWorkLog(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "업무일지가 삭제되었습니다."));
    }



} 