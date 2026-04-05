package com.showflix.api.admin.application;

import com.showflix.api.admin.domain.DailyNote;
import com.showflix.api.admin.mapper.DailyNoteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DailyNoteService {

    private final DailyNoteMapper mapper;

    public DailyNoteService(DailyNoteMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<DailyNote> getByYear(int year) {
        return mapper.findByYear(year);
    }

    @Transactional
    public void save(DailyNote note) {
        mapper.upsert(note);
    }
}
