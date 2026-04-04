package com.showflix.api.admin.application;

import com.showflix.api.admin.domain.BeerSelect;
import com.showflix.api.admin.domain.BeerSelectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BeerSelectService {

    private final BeerSelectRepository repository;

    public BeerSelectService(BeerSelectRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<BeerSelect> getAll() {
        return repository.findAll();
    }

    @Transactional
    public void create(String beerName, String brand, String category,
                       String notes, boolean active) {
        if (beerName == null || beerName.isBlank()) {
            throw new IllegalArgumentException("맥주명을 입력해주세요.");
        }

        BeerSelect entity = new BeerSelect();
        entity.setBeerName(beerName);
        entity.setBrand(brand);
        entity.setCategory(category);
        entity.setNotes(notes);
        entity.setActive(active);

        repository.insert(entity);
    }

    @Transactional
    public void update(Long id, String beerName, String brand, String category,
                       String notes, boolean active) {
        if (beerName == null || beerName.isBlank()) {
            throw new IllegalArgumentException("맥주명을 입력해주세요.");
        }

        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 맥주입니다: " + id));

        BeerSelect entity = new BeerSelect();
        entity.setId(id);
        entity.setBeerName(beerName);
        entity.setBrand(brand);
        entity.setCategory(category);
        entity.setNotes(notes);
        entity.setActive(active);

        repository.update(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 맥주입니다: " + id));
        repository.deleteById(id);
    }
}
