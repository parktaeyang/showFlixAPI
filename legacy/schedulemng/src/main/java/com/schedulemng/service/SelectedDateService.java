package com.schedulemng.service;

import com.schedulemng.entity.AdminNote;
import com.schedulemng.entity.SelectedDate;
import com.schedulemng.entity.DailyRemarks;
import com.schedulemng.repository.AdminNoteRepostory;
import com.schedulemng.repository.SelectedDateRepository;
import com.schedulemng.repository.DailyRemarksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SelectedDateService {
    private final SelectedDateRepository selectedDateRepository;
    private final AdminNoteRepostory adminNoteRepostory;
    private final DailyRemarksRepository dailyRemarksRepository;

    private static final String GLOBAL_ID = "GLOBAL";

    public void saveAll(List<SelectedDate> data) {
        selectedDateRepository.saveAll(data);
    }

    public List<SelectedDate> getDatesByMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return selectedDateRepository.findByDateBetween(start.toString(), end.toString());
    }

    public void save(SelectedDate selectedDate) {
        selectedDateRepository.save(selectedDate);
    }

    public int updateRoleByDateAndUserId(String date, String userId, String role, String remarks) {
        return selectedDateRepository.updateRoleByDateAndUserId(date, userId, role, remarks);
    }

    public int deleteByDateAndUserId(String date, String userId) {
        return selectedDateRepository.deleteByDateAndUserId(date, userId);
    }

    public void updateConfirmationByDate(String date, String confirmed) {
        selectedDateRepository.updateConfirmationByDate(date, confirmed);
    }

    public void saveAdminNote(AdminNote adminNote) {
        adminNote.setId(GLOBAL_ID);

        adminNoteRepostory.save(adminNote);
    }

    public AdminNote getAdminNote(){
        return adminNoteRepostory.findById(GLOBAL_ID);
    }

    // ===== 날짜별 비고 =====
    public String getDailyRemarks(String date) {
        return dailyRemarksRepository.findByDate(date)
                .map(DailyRemarks::getRemarks)
                .orElse("");
    }

    public void saveOrUpdateDailyRemarks(String date, String remarks) {
        if (remarks == null) return;
        dailyRemarksRepository.findByDate(date)
                .ifPresentOrElse(dr -> {
                    dr.setRemarks(remarks);
                    dailyRemarksRepository.save(dr);
                }, () -> dailyRemarksRepository.save(new DailyRemarks(date, remarks)));
    }
}