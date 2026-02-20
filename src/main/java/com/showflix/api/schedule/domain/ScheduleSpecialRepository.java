package com.showflix.api.schedule.domain;

import java.util.List;
import java.util.Optional;

/**
 * Domain Layer - 특수예약 저장소 Port
 */
public interface ScheduleSpecialRepository {

    List<ScheduleSpecial> findAll();

    Optional<ScheduleSpecial> findById(Long id);

    void insert(ScheduleSpecial scheduleSpecial);

    void update(ScheduleSpecial scheduleSpecial);

    void deleteById(Long id);
}
