package com.showflix.api.admin.infrastructure;

import com.showflix.api.admin.domain.HealthCert;
import com.showflix.api.admin.domain.HealthCertRepository;
import com.showflix.api.admin.mapper.HealthCertMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HealthCertMyBatisRepository implements HealthCertRepository {

    private final HealthCertMapper mapper;

    public HealthCertMyBatisRepository(HealthCertMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<HealthCert> findAllWithUserName() { return mapper.findAllWithUserName(); }

    @Override
    public void upsert(HealthCert healthCert) { mapper.upsert(healthCert); }
}
