package com.showflix.api.schedule.infrastructure;

import com.showflix.api.schedule.domain.SelectedDate;
import com.showflix.api.schedule.domain.SelectedDateRepository;
import com.showflix.api.schedule.mapper.SelectedDateMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure Layer - SelectedDate 저장소 구현체 (MyBatis)
 */
@Repository
public class SelectedDateMyBatisRepository implements SelectedDateRepository {

    private final SelectedDateMapper mapper;

    public SelectedDateMyBatisRepository(SelectedDateMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<SelectedDate> findByDateBetween(String start, String end) {
        return mapper.findByDateBetween(start, end);
    }

    @Override
    public void saveAll(List<SelectedDate> list) {
        for (SelectedDate sd : list) {
            mapper.insert(sd);
        }
    }

    @Override
    public int updateRoleByDateAndUserId(String date, String userId, String role, String remarks) {
        return mapper.updateRoleByDateAndUserId(date, userId, role, remarks);
    }

    @Override
    public int deleteByDateAndUserId(String date, String userId) {
        return mapper.deleteByDateAndUserId(date, userId);
    }

    @Override
    public int updateConfirmationByDate(String date, String confirmed) {
        return mapper.updateConfirmationByDate(date, confirmed);
    }
}
