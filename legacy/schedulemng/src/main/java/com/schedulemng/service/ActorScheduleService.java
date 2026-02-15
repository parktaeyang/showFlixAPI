package com.schedulemng.service;

import com.schedulemng.dto.ActorScheduleSummaryDTO;
import com.schedulemng.dto.DailyScheduleDTO;
import com.schedulemng.entity.Schedule;
import com.schedulemng.entity.User;
import com.schedulemng.repository.ScheduleRepository;
import com.schedulemng.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActorScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    
    /**
     * 특정 월의 배우별 스케줄 요약을 조회합니다.
     */
    public List<ActorScheduleSummaryDTO> getActorScheduleSummary(int year, int month) {
        // 배우 계정만 조회
        List<User> actors = userRepository.findByAccountType(User.AccountType.ACTOR);
        
        return actors.stream()
            .map(actor -> {
                List<Schedule> actorSchedules = scheduleRepository.findByUsernameAndYearAndMonth(
                    actor.getUsername(), year, month);
                
                // 총 시간 계산
                double totalHours = actorSchedules.stream()
                    .mapToDouble(Schedule::getHours)
                    .sum();
                
                // 총 일수 계산
                int totalDays = (int) actorSchedules.stream()
                    .mapToDouble(Schedule::getHours)
                    .filter(hours -> hours > 0)
                    .count();
                
                return ActorScheduleSummaryDTO.builder()
                    .userid(actor.getUserid())
                    .username(actor.getUsername())
                    .phoneNumber(actor.getPhoneNumber())
                    .totalDays(totalDays)
                    .totalHours(totalHours)
                    .month(String.format("%04d-%02d", year, month))
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 특정 월의 일별 스케줄 데이터를 조회합니다.
     */
    public List<DailyScheduleDTO> getMonthlyScheduleData(int year, int month) {
        List<Schedule> monthlySchedules = scheduleRepository.findByYearAndMonth(year, month);
        
        // 날짜별로 그룹화
        Map<LocalDate, List<Schedule>> schedulesByDate = monthlySchedules.stream()
            .collect(Collectors.groupingBy(Schedule::getDate));
        
        // 해당 월의 모든 날짜 생성
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        List<DailyScheduleDTO> result = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Schedule> daySchedules = schedulesByDate.getOrDefault(date, new ArrayList<>());
            
            // 배우별 시간 매핑
            Map<String, Double> actorSchedules = daySchedules.stream()
                .collect(Collectors.toMap(
                    Schedule::getUsername,
                    Schedule::getHours
                ));
            
            // 일별 총 시간 계산
            double dailyTotal = daySchedules.stream()
                .mapToDouble(Schedule::getHours)
                .sum();
            
            // 특이사항 (첫 번째 스케줄의 remarks 사용)
            String remarks = daySchedules.stream()
                .map(Schedule::getRemarks)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
            
            result.add(DailyScheduleDTO.builder()
                .date(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .dayOfWeek(getDayOfWeekKorean(date))
                .actorSchedules(actorSchedules)
                .dailyTotal(dailyTotal)
                .remarks(remarks)
                .build());
        }
        
        return result;
    }
    
    /**
     * 스케줄을 저장하거나 업데이트합니다.
     */
    public Schedule saveSchedule(LocalDate date, String username, double hours, String memo, String remarks) {
        Optional<Schedule> existingSchedule = scheduleRepository.findByDateAndUsername(date, username);
        
        if (existingSchedule.isPresent()) {
            Schedule schedule = existingSchedule.get();
            schedule.setHours(hours);
            schedule.setMemo(memo);
            schedule.setRemarks(remarks);
            return scheduleRepository.save(schedule);
        } else {
            Schedule newSchedule = new Schedule();
            newSchedule.setDate(date);
            newSchedule.setUsername(username);
            newSchedule.setHours(hours);
            newSchedule.setMemo(memo);
            newSchedule.setRemarks(remarks);
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