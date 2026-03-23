package com.showflix.api.schedule.infrastructure;

import com.showflix.api.schedule.domain.ScheduleSummary;
import com.showflix.api.schedule.domain.ScheduleSummaryRepository;
import com.showflix.api.schedule.mapper.ScheduleSummaryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure Layer - ScheduleSummaryRepository 구현체 (MyBatis)
 */
@Repository
public class ScheduleSummaryMyBatisRepository implements ScheduleSummaryRepository {

    private final ScheduleSummaryMapper mapper;

    public ScheduleSummaryMyBatisRepository(ScheduleSummaryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ScheduleSummary> findByMonth(String start, String end) {
        return mapper.findByMonth(start, end);
    }

    @Override
    public void upsert(ScheduleSummary summary) {
        mapper.upsert(summary);
    }
}
