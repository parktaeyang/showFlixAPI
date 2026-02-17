package com.showflix.api.schedule.application;

import com.showflix.api.auth.domain.User;
import com.showflix.api.auth.domain.UserRepository;
import com.showflix.api.schedule.application.command.DeleteScheduleCommand;
import com.showflix.api.schedule.application.command.SaveScheduleCommand;
import com.showflix.api.schedule.application.command.ScheduleTableQueryCommand;
import com.showflix.api.schedule.application.command.UpdateDailyRemarksCommand;
import com.showflix.api.schedule.domain.Schedule;
import com.showflix.api.schedule.domain.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application Layer - Schedule 유스케이스 서비스
 */
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    /**
     * 유스케이스 1: 월별 스케줄 테이블 조회
     * 행: 날짜, 열: 배우명, 값: 근무시간
     */
    @Transactional(readOnly = true)
    public ScheduleTableResult getScheduleTable(ScheduleTableQueryCommand command) {
        List<String> actorNames = userRepository.findActors()
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        List<Schedule> schedules = scheduleRepository.findByYearAndMonth(
                command.year(), command.month());

        // date → (username → hours) 맵으로 그룹화
        Map<String, Map<String, Double>> dateActorHoursMap = new LinkedHashMap<>();
        Map<String, String> dateRemarksMap = new HashMap<>();
        for (Schedule s : schedules) {
            dateActorHoursMap
                    .computeIfAbsent(s.getDate(), k -> new HashMap<>())
                    .put(s.getUsername(), s.getHours() != null ? s.getHours() : 0.0);
            if (s.getRemarks() != null && !s.getRemarks().isBlank()) {
                dateRemarksMap.put(s.getDate(), s.getRemarks());
            }
        }

        LocalDate startDate = LocalDate.of(command.year(), command.month(), 1);
        int daysInMonth = startDate.lengthOfMonth();

        Map<String, Double> columnTotals = new LinkedHashMap<>();
        for (String actor : actorNames) {
            columnTotals.put(actor, 0.0);
        }
        double grandTotal = 0.0;

        List<ScheduleRowResult> rows = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(command.year(), command.month(), day);
            String dateStr = date.toString();
            String dayOfWeek = date.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.KOREAN);

            Map<String, Double> actorHoursOnDate =
                    dateActorHoursMap.getOrDefault(dateStr, Collections.emptyMap());

            Map<String, String> actorHoursDisplay = new LinkedHashMap<>();
            double rowTotal = 0.0;
            for (String actor : actorNames) {
                Double h = actorHoursOnDate.get(actor);
                if (h != null && h > 0) {
                    actorHoursDisplay.put(actor, formatHours(h));
                    rowTotal += h;
                    columnTotals.merge(actor, h, Double::sum);
                    grandTotal += h;
                } else {
                    actorHoursDisplay.put(actor, "");
                }
            }

            rows.add(new ScheduleRowResult(
                    dateStr,
                    dayOfWeek,
                    actorHoursDisplay,
                    formatHours(rowTotal),
                    dateRemarksMap.getOrDefault(dateStr, "")
            ));
        }

        Map<String, String> columnTotalsDisplay = new LinkedHashMap<>();
        columnTotals.forEach((actor, total) ->
                columnTotalsDisplay.put(actor, formatHours(total)));

        return new ScheduleTableResult(
                command.year(),
                command.month(),
                actorNames,
                rows,
                columnTotalsDisplay,
                formatHours(grandTotal)
        );
    }

    /**
     * 유스케이스 2: 스케줄 저장/수정 (date + username 기준 upsert)
     */
    @Transactional
    public void saveSchedule(SaveScheduleCommand command) {
        Optional<Schedule> existing = scheduleRepository
                .findByDateAndUsername(command.date(), command.username());

        if (existing.isPresent()) {
            Schedule s = existing.get();
            s.setHours(command.hours());
            s.setRemarks(command.remarks());
            scheduleRepository.update(s);
        } else {
            Schedule s = new Schedule();
            s.setDate(command.date());
            s.setUsername(command.username());
            s.setHours(command.hours());
            s.setRemarks(command.remarks());
            scheduleRepository.insert(s);
        }
    }

    /**
     * 유스케이스 3: 스케줄 삭제
     */
    @Transactional
    public void deleteSchedule(DeleteScheduleCommand command) {
        int affected = scheduleRepository.deleteByDateAndUsername(
                command.date(), command.username());
        if (affected == 0) {
            throw new ScheduleException("삭제할 스케줄이 존재하지 않습니다.");
        }
    }

    /**
     * 유스케이스 4: 일별 특이사항 일괄 업데이트
     * 레거시의 N+1 루프 → 단일 UPDATE 쿼리로 개선
     */
    @Transactional
    public void updateDailyRemarks(UpdateDailyRemarksCommand command) {
        scheduleRepository.updateRemarksByDate(command.date(), command.remarks());
    }

    // --- Result 내부 클래스 ---

    public static class ScheduleTableResult {
        private final int year;
        private final int month;
        private final List<String> actorNames;
        private final List<ScheduleRowResult> rows;
        private final Map<String, String> columnTotals;
        private final String grandTotal;

        public ScheduleTableResult(int year, int month, List<String> actorNames,
                                   List<ScheduleRowResult> rows,
                                   Map<String, String> columnTotals,
                                   String grandTotal) {
            this.year = year;
            this.month = month;
            this.actorNames = actorNames;
            this.rows = rows;
            this.columnTotals = columnTotals;
            this.grandTotal = grandTotal;
        }

        public int getYear() { return year; }
        public int getMonth() { return month; }
        public List<String> getActorNames() { return actorNames; }
        public List<ScheduleRowResult> getRows() { return rows; }
        public Map<String, String> getColumnTotals() { return columnTotals; }
        public String getGrandTotal() { return grandTotal; }
    }

    public static class ScheduleRowResult {
        private final String date;
        private final String dayOfWeek;
        private final Map<String, String> actorHours;
        private final String rowTotal;
        private final String remarks;

        public ScheduleRowResult(String date, String dayOfWeek,
                                 Map<String, String> actorHours,
                                 String rowTotal, String remarks) {
            this.date = date;
            this.dayOfWeek = dayOfWeek;
            this.actorHours = actorHours;
            this.rowTotal = rowTotal;
            this.remarks = remarks;
        }

        public String getDate() { return date; }
        public String getDayOfWeek() { return dayOfWeek; }
        public Map<String, String> getActorHours() { return actorHours; }
        public String getRowTotal() { return rowTotal; }
        public String getRemarks() { return remarks; }
    }

    // --- private 헬퍼 ---

    private String formatHours(double hours) {
        if (hours == 0.0) return "";
        if (hours == Math.floor(hours)) {
            return String.valueOf((int) hours);
        }
        return String.valueOf(hours);
    }
}
