package com.schedulemng.service;

import com.schedulemng.dto.WorkLogDTO;
import com.schedulemng.entity.WorkLog;
import com.schedulemng.repository.WorkLogRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkLogService {

    private final WorkLogRepository workLogRepository;

    /**
     * 월별 업무일지 테이블 데이터 조회
     */
    public List<WorkLogDTO> getWorkLog(int year, int month) {
        try {
            log.info("월별 업무일지 테이블 조회: {}년 {}월", year, month);
            
            // 년월 형식 생성 (YYYY-MM)
            String yearMonth = String.format("%04d-%02d", year, month);

            // 해당 월의 모든 업무일지 조회
            List<WorkLog> workLogs = workLogRepository.findByYearMonth(yearMonth);

            return workLogs.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("월별 업무일지 테이블 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("업무일지 테이블 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 신규 업무일지 등록
     */
	public WorkLogDTO saveWorkLog(WorkLogDTO workLogDTO) {
        WorkLog workLog = toEntity(workLogDTO);
        WorkLog savedLog = workLogRepository.save(workLog);
        log.info("신규 업무일지 저장 완료: {}", savedLog);
        return toDto(savedLog);
    }

    /**
     * 업무일지 수정
     */
    public WorkLogDTO updateWorkLog(Long id, WorkLogDTO workLogDTO) {
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 업무일지를 찾을 수 없습니다."));

        // 엔티티 업데이트
        workLog.setDate(workLogDTO.getDate());
        workLog.setManager(workLogDTO.getManager());
        workLog.setCashPayment(workLogDTO.getCashPayment());
        workLog.setReservations(workLogDTO.getReservations());
        workLog.setEvent(workLogDTO.getEvent());
        workLog.setStoreRelated(workLogDTO.getStoreRelated());
        workLog.setNotes(workLogDTO.getNotes());

        return toDto(workLog);
    }

    /**
     * 업무일지 삭제
     */
    public void deleteWorkLog(Long id) {
        workLogRepository.deleteById(id);
    }

    /**
     * WorkLog 엔티티를 WorkLogDto로 변환하는 메소드
     */
    private WorkLogDTO toDto(WorkLog workLog) {
        return WorkLogDTO.builder()
                .id(workLog.getId())
                .date(workLog.getDate().toString())
                .manager(workLog.getManager())
                .cashPayment(workLog.getCashPayment())
                .reservations(workLog.getReservations())
                .event(workLog.getEvent())
                .storeRelated(workLog.getStoreRelated())
                .notes(workLog.getNotes())
                .build();
    }

    /**
     * WorkDto를 WorkLog 엔티티로 변환하는 메소드
     */
    private WorkLog toEntity(WorkLogDTO workLogDTO) {
        WorkLog workLog = new WorkLog();
        workLog.setDate(workLogDTO.getDate());
        workLog.setManager(workLogDTO.getManager());
        workLog.setCashPayment(workLogDTO.getCashPayment());
        workLog.setReservations(workLogDTO.getReservations());
        workLog.setEvent(workLogDTO.getEvent());
        workLog.setStoreRelated(workLogDTO.getStoreRelated());
        workLog.setNotes(workLogDTO.getNotes());

        return workLog;
    }

} 