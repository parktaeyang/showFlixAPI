package com.showflix.api.admin.application;

import com.showflix.api.admin.domain.HealthCert;
import com.showflix.api.admin.domain.HealthCertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HealthCertService {

    private final HealthCertRepository repository;

    public HealthCertService(HealthCertRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<HealthCert> getAll() {
        return repository.findAllWithUserName();
    }

    @Transactional
    public void update(String userId, String expireDate, String notes) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("사용자 ID를 입력해주세요.");
        }

        HealthCert entity = new HealthCert();
        entity.setUserId(userId);
        entity.setExpireDate(expireDate);
        entity.setNotes(notes);

        repository.upsert(entity);
    }
}
