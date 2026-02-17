package com.showflix.api.schedule.application.command;

import java.util.Map;

/**
 * Application Layer - 선택 날짜 저장 Command
 */
public record SaveSelectedDatesCommand(
        String userId,
        String userName,
        String role,
        Map<String, DateSelection> dateSelections
) {
    public record DateSelection(boolean openHope) {}
}
