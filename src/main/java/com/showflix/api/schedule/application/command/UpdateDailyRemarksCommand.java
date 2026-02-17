package com.showflix.api.schedule.application.command;

/**
 * Application Layer - 일별 특이사항 업데이트 Command
 */
public record UpdateDailyRemarksCommand(String date, String remarks) {}
