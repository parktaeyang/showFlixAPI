package com.showflix.api.schedule.application;

import com.showflix.api.auth.infrastructure.security.CustomUserDetails;
import com.showflix.api.schedule.application.command.MonthQueryCommand;
import com.showflix.api.schedule.application.command.SaveSelectedDatesCommand;
import com.showflix.api.schedule.domain.SelectedDate;
import com.showflix.api.schedule.domain.SelectedDateRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Application Layer - 선택 날짜 유스케이스 서비스
 */
@Service
public class SelectedDateService {

    private final SelectedDateRepository selectedDateRepository;

    public SelectedDateService(SelectedDateRepository selectedDateRepository) {
        this.selectedDateRepository = selectedDateRepository;
    }

    /**
     * 선택 날짜 저장
     */
    @Transactional
    public void saveSelectedDates(SaveSelectedDatesCommand command) {
        List<SelectedDate> list = new ArrayList<>();
        for (Map.Entry<String, SaveSelectedDatesCommand.DateSelection> e : command.dateSelections().entrySet()) {
            SelectedDate sd = new SelectedDate();
            sd.setDate(e.getKey());
            sd.setUserId(command.userId());
            sd.setUserName(command.userName());
            sd.setRole(command.role() != null ? command.role() : "");
            sd.setOpenHope(e.getValue().openHope());
            sd.setConfirmed("N");
            list.add(sd);
        }
        selectedDateRepository.saveAll(list);
    }

    /**
     * 월별 데이터 조회
     */
    @Transactional(readOnly = true)
    public MonthResult getDatesByMonth(MonthQueryCommand command) {
        LocalDate start = LocalDate.of(command.year(), command.month(), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<SelectedDate> list = selectedDateRepository.findByDateBetween(start.toString(), end.toString());

        boolean isAdmin = isCurrentUserAdmin();
        String currentUserId = getCurrentUserId();

        // 일반 사용자: 미확정은 본인 것만 노출
        if (!isAdmin && currentUserId != null) {
            list = list.stream()
                    .filter(d -> {
                        boolean confirmed = "Y".equalsIgnoreCase(d.getConfirmed());
                        return confirmed || currentUserId.equals(d.getUserId());
                    })
                    .toList();
        }

        return new MonthResult(isAdmin, list);
    }

    /**
     * 선택 날짜 단건 삭제
     */
    @Transactional
    public int deleteSelectedDate(String date, String userId) {
        return selectedDateRepository.deleteByDateAndUserId(date, userId);
    }

    /**
     * 관리자가 특정 날짜에 사용자 추가 (upsert)
     */
    @Transactional
    public void addUserToDate(String date, String userId, String userName, String role) {
        SelectedDate sd = new SelectedDate();
        sd.setDate(date);
        sd.setUserId(userId);
        sd.setUserName(userName != null ? userName : "");
        sd.setRole(role != null ? role : "");
        sd.setOpenHope(false);
        sd.setConfirmed("N");
        selectedDateRepository.saveAll(List.of(sd));
    }

    /**
     * 특정 날짜의 특정 사용자에 대한 역할/비고 업데이트 (관리자용)
     */
    @Transactional
    public void updateRoleAndRemarks(String date, String userId, String role, String remarks) {
        selectedDateRepository.updateRoleByDateAndUserId(date, userId, role, remarks);
    }

    public static class MonthResult {
        private final boolean admin;
        private final List<SelectedDate> data;

        public MonthResult(boolean admin, List<SelectedDate> data) {
            this.admin = admin;
            this.data = data;
        }

        public boolean isAdmin() {
            return admin;
        }

        public List<SelectedDate> getData() {
            return data;
        }
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            return false;
        }
        return cud.getUser().isAdmin();
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            return null;
        }
        return cud.getUser().getUserid();
    }
}
