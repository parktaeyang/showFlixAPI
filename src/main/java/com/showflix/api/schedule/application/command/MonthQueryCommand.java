package com.showflix.api.schedule.application.command;

/**
 * Application Layer - 월별 데이터 조회 Command
 */
public record MonthQueryCommand(int year, int month) {}
