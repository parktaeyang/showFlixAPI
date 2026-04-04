package com.showflix.api.admin.domain;

import java.util.List;
import java.util.Optional;

public interface BeerSelectRepository {

    List<BeerSelect> findAll();

    Optional<BeerSelect> findById(Long id);

    void insert(BeerSelect beerSelect);

    void update(BeerSelect beerSelect);

    void deleteById(Long id);
}
