package com.showflix.api.admin.infrastructure;

import com.showflix.api.admin.domain.XmasSeat;
import com.showflix.api.admin.domain.XmasSeatRepository;
import com.showflix.api.admin.mapper.XmasSeatMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class XmasSeatMyBatisRepository implements XmasSeatRepository {

    private final XmasSeatMapper mapper;

    public XmasSeatMyBatisRepository(XmasSeatMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<XmasSeat> findByEventDate(String eventDate) { return mapper.findByEventDate(eventDate); }

    @Override
    public Optional<XmasSeat> findById(Long id) { return Optional.ofNullable(mapper.findById(id)); }

    @Override
    public void insert(XmasSeat xmasSeat) { mapper.insert(xmasSeat); }

    @Override
    public void update(XmasSeat xmasSeat) { mapper.update(xmasSeat); }

    @Override
    public void deleteById(Long id) { mapper.deleteById(id); }
}
