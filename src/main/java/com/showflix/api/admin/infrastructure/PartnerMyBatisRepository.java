package com.showflix.api.admin.infrastructure;

import com.showflix.api.admin.domain.Partner;
import com.showflix.api.admin.domain.PartnerRepository;
import com.showflix.api.admin.mapper.PartnerMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PartnerMyBatisRepository implements PartnerRepository {

    private final PartnerMapper mapper;

    public PartnerMyBatisRepository(PartnerMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Partner> findAll() { return mapper.findAll(); }

    @Override
    public Optional<Partner> findById(Long id) { return Optional.ofNullable(mapper.findById(id)); }

    @Override
    public void insert(Partner partner) { mapper.insert(partner); }

    @Override
    public void update(Partner partner) { mapper.update(partner); }

    @Override
    public void deleteById(Long id) { mapper.deleteById(id); }
}
