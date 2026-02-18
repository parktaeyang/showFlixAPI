package com.showflix.api.schedule.application;

import com.showflix.api.schedule.application.command.ConfirmScheduleCommand;
import com.showflix.api.schedule.application.command.SaveTimeSlotCommand;
import com.showflix.api.schedule.domain.ScheduleTimeSlot;
import com.showflix.api.schedule.domain.ScheduleTimeSlotRepository;
import com.showflix.api.schedule.domain.SelectedDateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScheduleTimeSlotService {

    private final ScheduleTimeSlotRepository timeSlotRepository;
    private final SelectedDateRepository selectedDateRepository;

    public ScheduleTimeSlotService(ScheduleTimeSlotRepository timeSlotRepository,
                                   SelectedDateRepository selectedDateRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.selectedDateRepository = selectedDateRepository;
    }

    /** 특정 날짜의 시간표 조회 */
    @Transactional(readOnly = true)
    public List<ScheduleTimeSlot> getTimeSlotsByDate(String date) {
        return timeSlotRepository.findByScheduleDate(date);
    }

    /**
     * 시간표 저장 (배치)
     * 기존 슬롯을 전부 지우고 새로 저장 (단순화)
     */
    @Transactional
    public void saveTimeSlots(List<SaveTimeSlotCommand> commands) {
        if (commands == null || commands.isEmpty()) return;

        String date = commands.get(0).scheduleDate();
        timeSlotRepository.deleteByScheduleDate(date);

        for (SaveTimeSlotCommand cmd : commands) {
            ScheduleTimeSlot slot = new ScheduleTimeSlot();
            slot.setScheduleDate(cmd.scheduleDate());
            slot.setTimeSlot(cmd.timeSlot());
            slot.setTheme(cmd.theme());
            slot.setPerformer(cmd.performer());
            slot.setConfirmed("N");
            timeSlotRepository.save(slot);
        }
    }

    /**
     * 스케줄 확정/취소
     * - schedule_time_slot.confirmed 일괄 변경
     * - selected_date.confirmed 일괄 변경
     */
    @Transactional
    public void confirmSchedule(ConfirmScheduleCommand cmd) {
        timeSlotRepository.updateConfirmationByDate(cmd.date(), cmd.confirmed());
        selectedDateRepository.updateConfirmationByDate(cmd.date(), cmd.confirmed());
    }

    // ----------------------------------------------------------------
    // Inner result classes
    // ----------------------------------------------------------------

    public static class TimeSlotResult {
        private final String scheduleDate;
        private final String timeSlot;
        private final String theme;
        private final String performer;
        private final String confirmed;

        public TimeSlotResult(ScheduleTimeSlot slot) {
            this.scheduleDate = slot.getScheduleDate();
            this.timeSlot     = slot.getTimeSlot();
            this.theme        = slot.getTheme();
            this.performer    = slot.getPerformer();
            this.confirmed    = slot.getConfirmed();
        }

        public String getScheduleDate() { return scheduleDate; }
        public String getTimeSlot()     { return timeSlot; }
        public String getTheme()        { return theme; }
        public String getPerformer()    { return performer; }
        public String getConfirmed()    { return confirmed; }
    }
}
