package com.showflix.api.admin.infrastructure;

import com.showflix.api.admin.domain.Membership;
import com.showflix.api.admin.domain.MembershipRepository;
import com.showflix.api.admin.mapper.MembershipMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MembershipMyBatisRepository implements MembershipRepository {

    private final MembershipMapper mapper;

    public MembershipMyBatisRepository(MembershipMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Membership> findAll() { return mapper.findAll(); }

    @Override
    public Optional<Membership> findById(Long id) { return Optional.ofNullable(mapper.findById(id)); }

    @Override
    public void insert(Membership membership) { mapper.insert(membership); }

    @Override
    public void update(Membership membership) { mapper.update(membership); }

    @Override
    public void deleteById(Long id) { mapper.deleteById(id); }
}
