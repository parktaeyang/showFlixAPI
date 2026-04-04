package com.showflix.api.admin.infrastructure;

import com.showflix.api.admin.domain.BeerSelect;
import com.showflix.api.admin.domain.BeerSelectRepository;
import com.showflix.api.admin.mapper.BeerSelectMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BeerSelectMyBatisRepository implements BeerSelectRepository {

    private final BeerSelectMapper mapper;

    public BeerSelectMyBatisRepository(BeerSelectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<BeerSelect> findAll() { return mapper.findAll(); }

    @Override
    public Optional<BeerSelect> findById(Long id) { return Optional.ofNullable(mapper.findById(id)); }

    @Override
    public void insert(BeerSelect beerSelect) { mapper.insert(beerSelect); }

    @Override
    public void update(BeerSelect beerSelect) { mapper.update(beerSelect); }

    @Override
    public void deleteById(Long id) { mapper.deleteById(id); }
}
