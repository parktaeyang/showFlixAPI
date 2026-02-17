package com.showflix.api.schedule.application.command;

/**
 * Application Layer - 스케줄 삭제 Command
 */
public record DeleteScheduleCommand(String date, String username) {}
