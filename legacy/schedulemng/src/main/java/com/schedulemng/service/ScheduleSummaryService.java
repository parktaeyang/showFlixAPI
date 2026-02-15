package com.schedulemng.service;

import com.schedulemng.dto.ScheduleDto;
import com.schedulemng.dto.BatchResult;
import com.schedulemng.dto.ScheduleTableDTO;
import com.schedulemng.dto.ScheduleRowDTO;
import com.schedulemng.entity.ScheduleSummary;
import com.schedulemng.entity.DailyRemarks;
import com.schedulemng.entity.User;
import com.schedulemng.repository.ScheduleSummaryRepository;
import com.schedulemng.repository.DailyRemarksRepository;
import com.schedulemng.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleSummaryService {
    
    private final ScheduleSummaryRepository scheduleSummaryRepository;
    private final DailyRemarksRepository dailyRemarksRepository;
    private final UserRepository userRepository;
    
    /**
     * 개별 스케줄 저장
     */
    @Transactional
    public boolean saveSchedule(String date, String username, String hours) {
        try {
            log.info("스케줄 저장: {} - {} - {}", date, username, hours);
            
            // 사용자 조회
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.error("사용자를 찾을 수 없습니다: {}", username);
                return false;
            }
            
            User user = userOpt.get();
            
            // 기존 스케줄 조회 또는 새로 생성
            Optional<ScheduleSummary> existingSchedule = scheduleSummaryRepository.findByUserIdAndDate(user.getUserid(), date);
            
            ScheduleSummary schedule;
            if (existingSchedule.isPresent()) {
                schedule = existingSchedule.get();
                schedule.setHours(hours);
            } else {
                schedule = new ScheduleSummary(user.getUserid(), date, hours);
            }
            
            scheduleSummaryRepository.save(schedule);
            log.info("스케줄 저장 완료: {} - {} - {}", date, username, hours);
            return true;
            
        } catch (Exception e) {
            log.error("스케줄 저장 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 일별 특이사항 저장
     */
    @Transactional
    public boolean saveDailyRemarks(String date, String remarks) {
        try {
            log.info("일별 특이사항 저장: {} - {}", date, remarks);
            
            // 기존 특이사항 조회 또는 새로 생성
            Optional<DailyRemarks> existingRemarks = dailyRemarksRepository.findByDate(date);
            
            DailyRemarks dailyRemarks;
            if (existingRemarks.isPresent()) {
                dailyRemarks = existingRemarks.get();
                dailyRemarks.setRemarks(remarks);
            } else {
                dailyRemarks = new DailyRemarks(date, remarks);
            }
            
            dailyRemarksRepository.save(dailyRemarks);
            log.info("일별 특이사항 저장 완료: {} - {}", date, remarks);
            return true;
            
        } catch (Exception e) {
            log.error("일별 특이사항 저장 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 전체 스케줄 일괄 저장
     */
    @Transactional
    public BatchResult saveAllSchedules(List<ScheduleDto> schedules) {
        int totalCount = schedules.size();
        int successCount = 0;
        int failureCount = 0;
        
        log.info("전체 스케줄 일괄 저장 시작: {}개", totalCount);
        
        for (ScheduleDto scheduleDto : schedules) {
            boolean success = saveSchedule(
                scheduleDto.getDate(), 
                scheduleDto.getUsername(), 
                scheduleDto.getHours()
            );
            
            if (success) {
                successCount++;
            } else {
                failureCount++;
            }
        }
        
        boolean overallSuccess = failureCount == 0;
        BatchResult result = new BatchResult(overallSuccess, totalCount, successCount, failureCount);
        
        log.info("전체 스케줄 일괄 저장 완료: 성공 {}개, 실패 {}개", successCount, failureCount);
        return result;
    }
    
    /**
     * 월별 스케줄 테이블 데이터 조회
     */
    public ScheduleTableDTO getScheduleTable(int year, int month) {
        try {
            log.info("월별 스케줄 테이블 조회: {}년 {}월", year, month);
            
            // 년월 형식 생성 (YYYY-MM)
            String yearMonth = String.format("%04d-%02d", year, month);
            
            // 해당 월의 모든 날짜 생성
            List<String> allDates = generateDatesForMonth(year, month);
            
            // 배우 목록 조회
            List<User> actors = userRepository.findByAccountType(User.AccountType.ACTOR);
            List<String> actorNames = actors.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
            
            // 해당 월의 모든 스케줄 조회
            List<ScheduleSummary> schedules = scheduleSummaryRepository.findByYearMonth(yearMonth);
            
            // 해당 월의 모든 특이사항 조회
            List<DailyRemarks> dailyRemarks = dailyRemarksRepository.findByYearMonth(yearMonth);
            
            // 데이터 구조화
            Map<String, Map<String, String>> scheduleMap = schedules.stream()
                .collect(Collectors.groupingBy(
                    ScheduleSummary::getDate,
                    Collectors.toMap(
                        s -> actors.stream()
                            .filter(a -> a.getUserid().equals(s.getUserId()))
                            .findFirst()
                            .map(User::getUsername)
                            .orElse(""),
                        ScheduleSummary::getHours
                    )
                ));
            
            Map<String, String> remarksMap = dailyRemarks.stream()
                .collect(Collectors.toMap(
                    DailyRemarks::getDate,
                    DailyRemarks::getRemarks
                ));
            
            // 테이블 행 생성
            List<ScheduleRowDTO> rows = allDates.stream()
                .map(date -> {
                    String dayOfWeek = getDayOfWeek(date);
                    Map<String, String> actorHours = scheduleMap.getOrDefault(date, Map.of());
                    String remarks = remarksMap.getOrDefault(date, "");
                    
                    // 해당 날짜의 행 합계 계산
                    double rowTotal = 0.0;
                    for (String hours : actorHours.values()) {
                        if (hours != null && !hours.isEmpty()) {
                            try {
                                rowTotal += Double.parseDouble(hours);
                            } catch (NumberFormatException e) {
                                log.warn("잘못된 시간 형식: {}", hours);
                            }
                        }
                    }
                    
                    return ScheduleRowDTO.builder()
                        .date(date)
                        .dayOfWeek(dayOfWeek)
                        .actorHours(actorHours)
                        .rowTotal(String.format("%.1f", rowTotal))
                        .remarks(remarks)
                        .build();
                })
                .collect(Collectors.toList());
            
            // 합계 계산
            Map<String, String> columnTotals = calculateColumnTotals(actorNames, scheduleMap);
            String grandTotal = calculateGrandTotal(scheduleMap);
            
            ScheduleTableDTO result = new ScheduleTableDTO();
            result.setActorNames(actorNames);
            result.setRows(rows);
            result.setColumnTotals(columnTotals);
            result.setGrandTotal(grandTotal);
            
            log.info("월별 스케줄 테이블 조회 완료: 배우 {}명, 날짜 {}일", actorNames.size(), rows.size());
            return result;
            
        } catch (Exception e) {
            log.error("월별 스케줄 테이블 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("스케줄 테이블 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 해당 월의 모든 날짜 생성
     */
    private List<String> generateDatesForMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        return startDate.datesUntil(endDate.plusDays(1))
            .map(date -> date.format(formatter))
            .collect(Collectors.toList());
    }
    
    /**
     * 요일 계산
     */
    private String getDayOfWeek(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        String[] dayNames = {"월", "화", "수", "목", "금", "토", "일"};
        return dayNames[date.getDayOfWeek().getValue() - 1];
    }
    
    /**
     * 열별 합계 계산
     */
    private Map<String, String> calculateColumnTotals(List<String> actorNames, Map<String, Map<String, String>> scheduleMap) {
        Map<String, Double> totals = actorNames.stream()
            .collect(Collectors.toMap(name -> name, name -> 0.0));
        
        for (Map<String, String> dailySchedule : scheduleMap.values()) {
            for (String actorName : actorNames) {
                String hours = dailySchedule.get(actorName);
                if (hours != null && !hours.isEmpty()) {
                    try {
                        double hoursValue = Double.parseDouble(hours);
                        totals.put(actorName, totals.get(actorName) + hoursValue);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 시간 형식: {}", hours);
                    }
                }
            }
        }
        
        return totals.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> String.format("%.1f", entry.getValue())
            ));
    }
    
    /**
     * 전체 합계 계산
     */
    private String calculateGrandTotal(Map<String, Map<String, String>> scheduleMap) {
        double total = 0.0;
        
        for (Map<String, String> dailySchedule : scheduleMap.values()) {
            for (String hours : dailySchedule.values()) {
                if (hours != null && !hours.isEmpty()) {
                    try {
                        total += Double.parseDouble(hours);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 시간 형식: {}", hours);
                    }
                }
            }
        }
        
        return String.format("%.1f", total);
    }
    
    /**
     * 특정 사용자의 특정 월 출근 통계 조회
     * @param userId 사용자 ID
     * @param year 년도
     * @param month 월 (1-12)
     * @return Map containing totalDays (총 출근 일수) and totalHours (총 출근 시간)
     */
    public Map<String, Object> getUserMonthlyStats(String userId, int year, int month) {
        try {
            log.info("사용자 월별 출근 통계 조회: {} - {}년 {}월", userId, year, month);
            
            // 년월 형식 생성 (YYYY-MM)
            String yearMonth = String.format("%04d-%02d", year, month);
            
            // 해당 사용자의 해당 월 스케줄 조회
            List<ScheduleSummary> schedules = scheduleSummaryRepository.findByUserIdAndYearMonth(userId, yearMonth);
            
            // 총 출근 일수
            int totalDays = schedules.size();
            
            // 총 출근 시간 계산
            double totalHours = 0.0;
            for (ScheduleSummary schedule : schedules) {
                String hours = schedule.getHours();
                if (hours != null && !hours.isEmpty()) {
                    try {
                        totalHours += Double.parseDouble(hours);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 시간 형식: {} - {}", schedule.getDate(), hours);
                    }
                }
            }
            
            Map<String, Object> result = Map.of(
                "totalDays", totalDays,
                "totalHours", String.format("%.1f", totalHours)
            );
            
            log.info("사용자 월별 출근 통계: 총 {}일, 총 {}시간", totalDays, String.format("%.1f", totalHours));
            return result;
            
        } catch (Exception e) {
            log.error("사용자 월별 출근 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("출근 통계 조회 중 오류가 발생했습니다.", e);
        }
    }
} 