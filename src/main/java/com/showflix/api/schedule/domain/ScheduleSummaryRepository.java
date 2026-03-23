package com.showflix.api.schedule.domain;

import java.util.List;

/**
 * Domain Layer - ScheduleSummary 저장소 Port
 */
public interface ScheduleSummaryRepository {

    List<ScheduleSummary> findByMonth(String start, String end);

    void upsert(ScheduleSummary summary);
}
