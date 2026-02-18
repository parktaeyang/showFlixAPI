package com.showflix.api.schedule.application.command;

/**
 * 스케줄 확정 커맨드
 * - schedule_time_slot.confirmed = Y
 * - selected_date.confirmed = Y
 */
public record ConfirmScheduleCommand(
        String date,       // YYYY-MM-DD
        String confirmed   // "Y" or "N"
) {}
