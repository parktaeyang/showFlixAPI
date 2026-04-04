package com.showflix.api.admin.application;

import com.showflix.api.admin.domain.DailyStatusEntry;
import com.showflix.api.admin.mapper.DailyStatusMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DailyStatusService {

    private final DailyStatusMapper mapper;

    public DailyStatusService(DailyStatusMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<DailyStatusEntry> getByDate(String date) {
        return mapper.findByDate(date);
    }
}
