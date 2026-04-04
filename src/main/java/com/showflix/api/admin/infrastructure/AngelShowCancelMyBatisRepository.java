package com.showflix.api.admin.infrastructure;

import com.showflix.api.admin.domain.AngelShowCancel;
import com.showflix.api.admin.domain.AngelShowCancelRepository;
import com.showflix.api.admin.mapper.AngelShowCancelMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Infrastructure Layer - AngelShowCancelRepository 구현체 (MyBatis)
 */
@Repository
public class AngelShowCancelMyBatisRepository implements AngelShowCancelRepository {

    private final AngelShowCancelMapper mapper;

    public AngelShowCancelMyBatisRepository(AngelShowCancelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AngelShowCancel> findByYearAndMonth(int year, int month) {
        return mapper.findByYearAndMonth(year, month);
    }

    @Override
    public Optional<AngelShowCancel> findById(Long id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    @Override
    public void insert(AngelShowCancel angelShowCancel) {
        mapper.insert(angelShowCancel);
    }

    @Override
    public void update(AngelShowCancel angelShowCancel) {
        mapper.update(angelShowCancel);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
