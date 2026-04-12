package com.showflix.api.schedule.application.command;

import java.util.List;

/**
 * 스케줄 통합 확정 커맨드 (시간표 저장 + 역할 저장 + 확정 처리를 단일 트랜잭션으로)
 */
public record ConfirmAllCommand(
        String date,
        List<SlotItem> slots,
        List<RoleItem> roles
) {
    public record SlotItem(String timeSlot, String theme, String performer) {}
    public record RoleItem(String userId, String role, String remarks) {}
}
