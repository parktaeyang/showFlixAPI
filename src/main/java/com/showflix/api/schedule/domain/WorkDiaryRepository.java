package com.showflix.api.schedule.domain;

import java.util.List;
import java.util.Optional;

/**
 * Domain Layer - 업무일지 저장소 Port
 */
public interface WorkDiaryRepository {

    List<WorkDiary> findByMonth(String yearMonth);

    Optional<WorkDiary> findById(Long id);

    void save(WorkDiary workDiary);

    void update(WorkDiary workDiary);

    void deleteById(Long id);
}
