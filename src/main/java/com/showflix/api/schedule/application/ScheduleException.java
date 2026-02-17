package com.showflix.api.schedule.application;

/**
 * Application Layer - Schedule 도메인 예외
 */
public class ScheduleException extends RuntimeException {

    public ScheduleException(String message) {
        super(message);
    }
}
