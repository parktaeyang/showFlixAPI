package com.showflix.api.schedule.application;

import com.showflix.api.auth.domain.User;
import com.showflix.api.auth.domain.UserRepository;
import com.showflix.api.schedule.domain.ScheduleSummary;
import com.showflix.api.schedule.domain.ScheduleSummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application Layer - 출근시간 요약 서비스
 */
@Service
public class ScheduleSummaryService {

    private final ScheduleSummaryRepository repository;
    private final UserRepository userRepository;

    public ScheduleSummaryService(ScheduleSummaryRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    /**
     * 월별 데이터 조회 결과 DTO
     */
    public record MonthResult(
            int year,
            int month,
            int daysInMonth,
            List<UserInfo> users,
            Map<String, Map<String, String>> data,  // userId -> (date -> hours)
            Map<String, String> dateRemarks          // date -> remarks (날짜별 특이사항)
    ) {}

    public record UserInfo(String userId, String userName, String accountType) {}

    /**
     * 월별 전체 출근시간 데이터 조회
     * - ACTOR, STAFF 유형만 포함
     * - 데이터가 없는 셀은 data 맵에 포함하지 않음 (프론트에서 '-' 처리)
     */
    @Transactional(readOnly = true)
    public MonthResult getMonthData(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        // ACTOR, STAFF 유형 직원만 필터링, username 기준 정렬
        List<UserInfo> users = userRepository.findAll().stream()
                .filter(u -> "ACTOR".equals(u.getAccountType()) || "STAFF".equals(u.getAccountType()))
                .sorted(Comparator.comparing(User::getUsername))
                .map(u -> new UserInfo(u.getUserid(), u.getUsername(), u.getAccountType()))
                .collect(Collectors.toList());

        // 해당 월 schedule_summary 데이터 조회
        List<ScheduleSummary> summaries = repository.findByMonth(start.toString(), end.toString());

        // userId -> (date -> hours) 맵 구성 + 날짜별 특이사항 추출
        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        Map<String, String> dateRemarks = new LinkedHashMap<>();
        for (ScheduleSummary s : summaries) {
            // __remarks__ 사용자: 날짜별 특이사항
            if ("__remarks__".equals(s.getUserId())) {
                if (s.getRemarks() != null && !s.getRemarks().isBlank()) {
                    dateRemarks.put(s.getDate(), s.getRemarks());
                }
                continue;
            }
            // hours 맵
            if (s.getHours() != null && !s.getHours().isBlank() && !"0".equals(s.getHours())) {
                data.computeIfAbsent(s.getUserId(), k -> new LinkedHashMap<>())
                    .put(s.getDate(), s.getHours());
            }
        }

        return new MonthResult(year, month, end.getDayOfMonth(), users, data, dateRemarks);
    }

    /**
     * 출근시간 일괄 저장 (변경된 셀만)
     */
    @Transactional
    public void saveBulk(List<SaveItem> items) {
        for (SaveItem item : items) {
            ScheduleSummary summary = new ScheduleSummary();
            summary.setUserId(item.userId());
            summary.setDate(item.date());
            // hours가 null/빈값이면 "0" 저장
            summary.setHours((item.hours() == null || item.hours().isBlank()) ? "0" : item.hours());
            summary.setRemarks(item.remarks());
            repository.upsert(summary);
        }
    }

    /**
     * 저장 요청 단건 DTO
     */
    public record SaveItem(String userId, String date, String hours, String remarks) {}
}
