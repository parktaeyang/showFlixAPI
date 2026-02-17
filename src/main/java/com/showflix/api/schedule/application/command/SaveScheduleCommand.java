package com.showflix.api.schedule.application.command;

/**
 * Application Layer - 스케줄 저장/수정 Command
 * date + username 기준 upsert
 */
public record SaveScheduleCommand(
        String date,
        String username,
        Double hours,
        String remarks
) {}
