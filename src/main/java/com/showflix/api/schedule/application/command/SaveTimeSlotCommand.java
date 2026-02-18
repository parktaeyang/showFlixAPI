package com.showflix.api.schedule.application.command;

/**
 * 시간표 슬롯 저장 커맨드
 */
public record SaveTimeSlotCommand(
        String scheduleDate,  // YYYY-MM-DD
        String timeSlot,      // HH:mm
        String theme,
        String performer      // 콤마 구분 출연자
) {}
