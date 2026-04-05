package com.showflix.api.admin.application;

import com.showflix.api.admin.domain.MonthlyNote;
import com.showflix.api.admin.mapper.MonthlyNoteMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlyNoteService {

    private final MonthlyNoteMapper mapper;

    public MonthlyNoteService(MonthlyNoteMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public MonthlyNote getByYearMonth(int year, int month) {
        return mapper.findByYearMonth(year, month);
    }

    @Transactional
    public void save(MonthlyNote note) {
        mapper.upsert(note);
    }
}
