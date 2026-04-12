package com.showflix.api.schedule.application;

import com.showflix.api.schedule.application.command.ConfirmAllCommand;
import com.showflix.api.schedule.application.command.ConfirmScheduleCommand;
import com.showflix.api.schedule.application.command.SaveTimeSlotCommand;
import com.showflix.api.schedule.domain.ScheduleTimeSlot;
import com.showflix.api.schedule.domain.ScheduleTimeSlotRepository;
import com.showflix.api.schedule.domain.SelectedDateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 삭제 전 기존 confirmed 상태를 보존하고 재삽입 시 복원
     */
    @Transactional
    public void saveTimeSlots(List<SaveTimeSlotCommand> commands) {
        if (commands == null || commands.isEmpty()) return;

        String date = commands.get(0).scheduleDate();

        // 기존 확정 상태 조회 → Map으로 보존
        Map<String, String> existingConfirmed = timeSlotRepository
                .findByScheduleDate(date).stream()
                .collect(Collectors.toMap(
                        ScheduleTimeSlot::getTimeSlot,
                        ScheduleTimeSlot::getConfirmed,
                        (a, b) -> a
                ));

        timeSlotRepository.deleteByScheduleDate(date);

        for (SaveTimeSlotCommand cmd : commands) {
            ScheduleTimeSlot slot = new ScheduleTimeSlot();
            slot.setScheduleDate(cmd.scheduleDate());
            slot.setTimeSlot(cmd.timeSlot());
            slot.setTheme(cmd.theme());
            slot.setPerformer(cmd.performer());
            // 기존 확정 상태 복원 (없으면 'N')
            slot.setConfirmed(existingConfirmed.getOrDefault(cmd.timeSlot(), "N"));
            timeSlotRepository.save(slot);
        }
    }

    /**
     * 통합 확정: 시간표 저장 + 역할/비고 저장 + 확정 처리 (단일 트랜잭션)
     */
    @Transactional
    public void confirmAll(ConfirmAllCommand cmd) {
        String date = cmd.date();

        // 1. 시간표 슬롯 저장
        if (cmd.slots() != null && !cmd.slots().isEmpty()) {
            List<SaveTimeSlotCommand> slotCommands = cmd.slots().stream()
                    .map(s -> new SaveTimeSlotCommand(date, s.timeSlot(), s.theme(), s.performer()))
                    .toList();
            saveTimeSlots(slotCommands);
        }

        // 2. 역할/비고 저장
        if (cmd.roles() != null) {
            for (ConfirmAllCommand.RoleItem role : cmd.roles()) {
                selectedDateRepository.updateRoleByDateAndUserId(
                        date, role.userId(), role.role(), role.remarks());
            }
        }

        // 3. 확정 처리
        confirmSchedule(new ConfirmScheduleCommand(date, "Y"));
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
