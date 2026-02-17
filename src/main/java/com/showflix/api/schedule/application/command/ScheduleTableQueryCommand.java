package com.showflix.api.schedule.application.command;

/**
 * Application Layer - 월별 스케줄 테이블 조회 Command
 */
public record ScheduleTableQueryCommand(int year, int month) {}
