package com.showflix.api.admin.domain;

import java.util.List;
import java.util.Optional;

/**
 * Domain Layer - 엔젤쇼 취소현황 저장소 Port
 */
public interface AngelShowCancelRepository {

    List<AngelShowCancel> findByYearAndMonth(int year, int month);

    Optional<AngelShowCancel> findById(Long id);

    void insert(AngelShowCancel angelShowCancel);

    void update(AngelShowCancel angelShowCancel);

    void deleteById(Long id);
}
