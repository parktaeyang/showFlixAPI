package com.showflix.api.schedule.infrastructure;

import com.showflix.api.schedule.domain.AdminNote;
import com.showflix.api.schedule.domain.AdminNoteRepository;
import com.showflix.api.schedule.mapper.AdminNoteMapper;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure Layer - AdminNote 저장소 구현체 (MyBatis)
 */
@Repository
public class AdminNoteMyBatisRepository implements AdminNoteRepository {

    private final AdminNoteMapper mapper;

    public AdminNoteMyBatisRepository(AdminNoteMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AdminNote findById(String id) {
        return mapper.findById(id);
    }

    @Override
    public void save(AdminNote adminNote) {
        mapper.upsert(adminNote);
    }
}
