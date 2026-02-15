package com.schedulemng.service;

import com.schedulemng.dto.ScheduleTableDTO;
import com.schedulemng.dto.ScheduleRowDTO;
import com.schedulemng.entity.Schedule;
import com.schedulemng.entity.User;
import com.schedulemng.repository.ScheduleRepository;
import com.schedulemng.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    
    /**
     * 특정 월의 스케줄 테이블 데이터를 조회합니다.
     */
    public ScheduleTableDTO getScheduleTable(int year, int month) {
        // 배우 계정만 조회
        List<User> actors = userRepository.findByAccountType(User.AccountType.ACTOR);
        List<String> actorNames = actors.stream()
            .map(User::getUsername)
            .sorted()
            .collect(Collectors.toList());
        
        // 해당 월의 모든 스케줄 조회
        List<Schedule> monthlySchedules = scheduleRepository.findByYearAndMonth(year, month);
        
        // 날짜별로 그룹화
        Map<LocalDate, List<Schedule>> schedulesByDate = monthlySchedules.stream()
            .collect(Collectors.groupingBy(Schedule::getDate));
        
        // 해당 월의 모든 날짜 생성
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        List<ScheduleRowDTO> rows = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Schedule> daySchedules = schedulesByDate.getOrDefault(date, new ArrayList<>());
            
            // 배우별 시간 매핑
            Map<String, Double> actorHours = daySchedules.stream()
                .collect(Collectors.toMap(
                    Schedule::getUsername,
                    Schedule::getHours
                ));
            
            // 일별 총 시간 계산
            Double rowTotal = daySchedules.stream()
                .mapToDouble(Schedule::getHours)
                .sum();
            
            // 특이사항 (첫 번째 스케줄의 remarks 사용)
            String remarks = daySchedules.stream()
                .map(Schedule::getRemarks)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
            
            rows.add(ScheduleRowDTO.builder()
                .date(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .dayOfWeek(getDayOfWeekKorean(date))
                .actorHours(actorHours.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.valueOf(entry.getValue())
                    )))
                .rowTotal(String.valueOf(rowTotal))
                .remarks(remarks)
                .build());
        }
        
        // 배우별 총 시간 계산
        Map<String, Double> columnTotals = new HashMap<>();
        for (String actorName : actorNames) {
            Double total = scheduleRepository.findTotalHoursByUsernameAndYearAndMonth(actorName, year, month);
            columnTotals.put(actorName, total != null ? total : 0.0);
        }
        
        // 전체 총 시간 계산
        Double grandTotal = columnTotals.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        
        return ScheduleTableDTO.builder()
            .year(year)
            .month(month)
            .actorNames(actorNames)
            .rows(rows)
            .columnTotals(columnTotals.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> String.valueOf(entry.getValue())
                )))
            .grandTotal(String.valueOf(grandTotal))
            .build();
    }
    
    /**
     * 스케줄을 저장하거나 업데이트합니다.
     */
    public Schedule saveSchedule(LocalDate date, String username, Double hours, String remarks) {
        Optional<Schedule> existingSchedule = scheduleRepository.findByDateAndUsername(date, username);
        
        if (existingSchedule.isPresent()) {
            Schedule schedule = existingSchedule.get();
            schedule.setHours(hours);
            schedule.setRemarks(remarks);
            return scheduleRepository.save(schedule);
        } else {
            Schedule newSchedule = Schedule.builder()
                .date(date)
                .username(username)
                .hours(hours)
                .remarks(remarks)
                .build();
            return scheduleRepository.save(newSchedule);
        }
    }
    
    /**
     * 스케줄을 삭제합니다.
     */
    public void deleteSchedule(LocalDate date, String username) {
        scheduleRepository.deleteByDateAndUsername(date, username);
    }
    
    /**
     * 특정 날짜의 특이사항을 업데이트합니다.
     */
    public void updateDailyRemarks(LocalDate date, String remarks) {
        List<Schedule> daySchedules = scheduleRepository.findByDateOrderByUsername(date);
        
        for (Schedule schedule : daySchedules) {
            schedule.setRemarks(remarks);
            scheduleRepository.save(schedule);
        }
    }
    
    /**
     * 한국어 요일을 반환합니다.
     */
    private String getDayOfWeekKorean(LocalDate date) {
        String[] dayNames = {"월", "화", "수", "목", "금", "토", "일"};
        return dayNames[date.getDayOfWeek().getValue() - 1];
    }
} 