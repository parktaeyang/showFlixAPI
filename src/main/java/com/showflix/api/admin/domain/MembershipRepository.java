package com.showflix.api.admin.domain;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository {

    List<Membership> findAll();

    Optional<Membership> findById(Long id);

    void insert(Membership membership);

    void update(Membership membership);

    void deleteById(Long id);
}
