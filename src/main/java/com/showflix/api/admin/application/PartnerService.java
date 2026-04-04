package com.showflix.api.admin.application;

import com.showflix.api.admin.domain.Partner;
import com.showflix.api.admin.domain.PartnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartnerService {

    private final PartnerRepository repository;

    public PartnerService(PartnerRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Partner> getAll() {
        return repository.findAll();
    }

    @Transactional
    public void create(String category, String name, String contact,
                       String manager, String notes) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("업체명을 입력해주세요.");
        }

        Partner entity = new Partner();
        entity.setCategory(category);
        entity.setName(name);
        entity.setContact(contact);
        entity.setManager(manager);
        entity.setNotes(notes);

        repository.insert(entity);
    }

    @Transactional
    public void update(Long id, String category, String name, String contact,
                       String manager, String notes) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("업체명을 입력해주세요.");
        }

        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 업체입니다: " + id));

        Partner entity = new Partner();
        entity.setId(id);
        entity.setCategory(category);
        entity.setName(name);
        entity.setContact(contact);
        entity.setManager(manager);
        entity.setNotes(notes);

        repository.update(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 업체입니다: " + id));
        repository.deleteById(id);
    }
}
