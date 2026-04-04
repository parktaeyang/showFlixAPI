package com.showflix.api.admin.domain;

import java.util.List;
import java.util.Optional;

public interface XmasSeatRepository {

    List<XmasSeat> findByEventDate(String eventDate);

    Optional<XmasSeat> findById(Long id);

    void insert(XmasSeat xmasSeat);

    void update(XmasSeat xmasSeat);

    void deleteById(Long id);
}
