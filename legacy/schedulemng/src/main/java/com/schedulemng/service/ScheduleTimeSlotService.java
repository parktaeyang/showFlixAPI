package com.schedulemng.service;

import com.schedulemng.entity.ScheduleTimeSlot;
import com.schedulemng.entity.ScheduleTimeSlotId;
import com.schedulemng.repository.ScheduleTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 처리를 위해 추가

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor // final 필드를 사용하는 생성자를 자동으로 생성하여 의존성 주입
public class ScheduleTimeSlotService {

    private final ScheduleTimeSlotRepository scheduleTimeSlotRepository;

    /**
     * 특정 날짜의 모든 시간표 데이터를 조회.
     *
     * @param date 조회할 날짜 (YYYY-MM-DD 형식의 문자열)
     * @return 해당 날짜의 시간표 목록
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정하여 성능 최적화
    public List<ScheduleTimeSlot> getTimeSlotsByDate(String date) {
        String normalizedDate = normalizeDateFormat(date);
        return scheduleTimeSlotRepository.findByIdScheduleDateOrderByIdTimeSlotAsc(normalizedDate);
    }

    /**
     * 시간표 데이터를 저장하거나 업데이트.
     *
     * @param dateStr 해당 시간표 데이터의 날짜 (YYYY-MM-DD 형식의 문자열)
     * @param timeSlotDataList 시간, 테마, 출연자 정보를 담은 Map 리스트
     */
    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    public void saveOrUpdateScheduleTimeSlots(String dateStr, List<Map<String, String>> timeSlotDataList) {
        String normalizedDate = normalizeDateFormat(dateStr);

        for (Map<String, String> data : timeSlotDataList) {
            String timeStr = data.get("time");
            
            // 시간 형식을 "HH:mm"으로 정규화 (초 제거)
            String normalizedTime = normalizeTimeFormat(timeStr);
            String theme = data.get("theme");
            String performer = data.get("performer");

            ScheduleTimeSlotId id = new ScheduleTimeSlotId(normalizedDate, normalizedTime);

            // 기존 데이터가 있는지 확인
            Optional<ScheduleTimeSlot> existingSlotOptional = scheduleTimeSlotRepository.findById(id);
            ScheduleTimeSlot scheduleTimeSlot;

            if (existingSlotOptional.isPresent()) {
                // 기존 데이터가 있으면 업데이트
                scheduleTimeSlot = existingSlotOptional.get();
            } else {
                // 없으면 새로운 엔티티 생성 및 ID 설정
                scheduleTimeSlot = new ScheduleTimeSlot();
                scheduleTimeSlot.setId(id);
            }

            // 테마와 출연자 정보 설정
            scheduleTimeSlot.setTheme(theme);
            scheduleTimeSlot.setPerformer(performer);

            // 데이터베이스에 저장 또는 업데이트
            try {
                scheduleTimeSlotRepository.save(scheduleTimeSlot);
            } catch (Exception e) {
                // 중복 키 오류인 경우 기존 데이터를 업데이트
                if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("PRIMARY")) {
                    System.out.println("중복 키 감지, 기존 데이터 업데이트 시도");
                    // 다시 한번 기존 데이터를 조회해서 업데이트
                    Optional<ScheduleTimeSlot> existing = scheduleTimeSlotRepository.findById(id);
                    if (existing.isPresent()) {
                        ScheduleTimeSlot existingSlot = existing.get();
                        existingSlot.setTheme(theme);
                        existingSlot.setPerformer(performer);
                        scheduleTimeSlotRepository.save(existingSlot);
                        System.out.println("기존 데이터 업데이트 완료: " + id);
                    } else {
                        System.err.println("중복 키 오류 발생했지만 기존 데이터를 찾을 수 없음: " + id);
                        // 강제로 삭제 후 재생성 (최후의 수단)
                        try {
                            scheduleTimeSlotRepository.deleteById(id);
                            scheduleTimeSlotRepository.save(scheduleTimeSlot);
                            System.out.println("강제 재생성 완료: " + id);
                        } catch (Exception deleteException) {
                            System.err.println("강제 재생성도 실패: " + deleteException.getMessage());
                            throw e; // 원래 오류를 다시 던지기
                        }
                    }
                } else {
                    throw e; // 다른 오류는 다시 던지기
                }
            }
        }
    }

    /**
     * 날짜 형식을 "YYYY-MM-DD"로 정규화
     *
     * @param dateStr 날짜 문자열 (예: "2025-09-16", "2025-9-16")
     * @return 정규화된 날짜 문자열 (예: "2025-09-16")
     */
    private String normalizeDateFormat(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDate.now().toString();
        }
        
        // 공백 제거
        dateStr = dateStr.trim();
        
        // 이미 "YYYY-MM-DD" 형식인지 확인
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return dateStr;
        }
        
        // "YYYY-M-D" 형식을 "YYYY-MM-DD"로 변환
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.toString();
        } catch (Exception e) {
            System.err.println("날짜 파싱 실패: " + dateStr + ", 기본값 사용");
            return LocalDate.now().toString();
        }
    }

    /**
     * 시간 형식을 "HH:mm"으로 정규화 (초 제거)
     *
     * @param timeStr 시간 문자열 (예: "16:00", "16:00:00", "16:00:00.000")
     * @return 정규화된 시간 문자열 (예: "16:00")
     */
    private String normalizeTimeFormat(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return "00:00";
        }
        
        // 공백 제거
        timeStr = timeStr.trim();
        
        // "HH:mm:ss" 또는 "HH:mm:ss.SSS" 형식을 "HH:mm"으로 변환
        if (timeStr.contains(":")) {
            String[] parts = timeStr.split(":");
            if (parts.length >= 2) {
                return parts[0] + ":" + parts[1];
            }
        }
        
        return timeStr;
    }

    /**
     * 특정 날짜의 모든 시간표 확정 상태를 변경.
     *
     * @param dateStr 날짜 (YYYY-MM-DD 형식의 문자열)
     * @param confirmed 확정 여부 ("Y" 또는 "N")
     */
    @Transactional
    public void updateTimeSlotsConfirmationByDate(String dateStr, String confirmed) {
        String normalizedDate = normalizeDateFormat(dateStr);
        scheduleTimeSlotRepository.updateConfirmationByDate(normalizedDate, confirmed);
    }

    /**
     * 특정 날짜의 모든 시간표를 확정 상태로 변경.
     * @deprecated updateTimeSlotsConfirmationByDate 사용 권장
     */
    @Deprecated
    @Transactional
    public void confirmTimeSlotsByDate(String dateStr) {
        updateTimeSlotsConfirmationByDate(dateStr, "Y");
    }

    /**
     * 특정 날짜의 모든 시간표를 미확정 상태로 변경.
     * @deprecated updateTimeSlotsConfirmationByDate 사용 권장
     */
    @Deprecated
    @Transactional
    public void unconfirmTimeSlotsByDate(String dateStr) {
        updateTimeSlotsConfirmationByDate(dateStr, "N");
    }

    /**
     * 해당 날짜가 확정되었는지 여부 반환 (리포지토리 확장 없이 조회 결과로 판단)
     */
    @Transactional(readOnly = true)
    public boolean isDateConfirmed(String dateStr) {
        String normalizedDate = normalizeDateFormat(dateStr);
        List<ScheduleTimeSlot> list = scheduleTimeSlotRepository
                .findByIdScheduleDateOrderByIdTimeSlotAsc(normalizedDate);
        for (ScheduleTimeSlot s : list) {
            if ("Y".equalsIgnoreCase(s.getConfirmed())) {
                return true;
            }
        }
        return false;
    }
}