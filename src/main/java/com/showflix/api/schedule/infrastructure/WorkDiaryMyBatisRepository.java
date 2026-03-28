package com.showflix.api.schedule.infrastructure;

import com.showflix.api.schedule.domain.WorkDiary;
import com.showflix.api.schedule.domain.WorkDiaryRepository;
import com.showflix.api.schedule.mapper.WorkDiaryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Infrastructure Layer - WorkDiaryRepository 구현체 (MyBatis)
 */
@Repository
public class WorkDiaryMyBatisRepository implements WorkDiaryRepository {

    private final WorkDiaryMapper mapper;

    public WorkDiaryMyBatisRepository(WorkDiaryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<WorkDiary> findByMonth(String yearMonth) {
        return mapper.findByMonth(yearMonth);
    }

    @Override
    public Optional<WorkDiary> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Override
    public void save(WorkDiary workDiary) {
        mapper.insert(workDiary);
    }

    @Override
    public void update(WorkDiary workDiary) {
        mapper.update(workDiary);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
