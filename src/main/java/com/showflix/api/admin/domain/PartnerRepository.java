package com.showflix.api.admin.domain;

import java.util.List;
import java.util.Optional;

/**
 * Domain Layer - 협력업체 저장소 Port
 */
public interface PartnerRepository {

    List<Partner> findAll();

    Optional<Partner> findById(Long id);

    void insert(Partner partner);

    void update(Partner partner);

    void deleteById(Long id);
}
