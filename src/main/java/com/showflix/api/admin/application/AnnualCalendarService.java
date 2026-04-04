package com.showflix.api.admin.application;

import com.showflix.api.admin.domain.AnnualCalendarEntry;
import com.showflix.api.admin.mapper.AnnualCalendarMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnnualCalendarService {

    private final AnnualCalendarMapper mapper;

    public AnnualCalendarService(AnnualCalendarMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AnnualCalendarEntry> getByYear(int year) {
        return mapper.findByYear(year);
    }
}
