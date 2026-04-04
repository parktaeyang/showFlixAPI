package com.showflix.api.admin.application;

import com.showflix.api.admin.domain.AngelShowCancel;
import com.showflix.api.admin.domain.AngelShowCancelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Layer - 엔젤쇼 취소현황 서비스
 */
@Service
public class AngelShowCancelService {

    private final AngelShowCancelRepository repository;

    public AngelShowCancelService(AngelShowCancelRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AngelShowCancel> getByMonth(int year, int month) {
        return repository.findByYearAndMonth(year, month);
    }

    @Transactional
    public void create(String cancelDate, String showTime, String reason,
                       String actorName, String notes) {
        if (cancelDate == null || cancelDate.isBlank()) {
            throw new IllegalArgumentException("취소 날짜를 입력해주세요.");
        }

        AngelShowCancel entity = new AngelShowCancel();
        entity.setCancelDate(cancelDate);
        entity.setShowTime(showTime);
        entity.setReason(reason);
        entity.setActorName(actorName);
        entity.setNotes(notes);

        repository.insert(entity);
    }

    @Transactional
    public void update(Long id, String cancelDate, String showTime, String reason,
                       String actorName, String notes) {
        if (cancelDate == null || cancelDate.isBlank()) {
            throw new IllegalArgumentException("취소 날짜를 입력해주세요.");
        }

        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 데이터입니다: " + id));

        AngelShowCancel entity = new AngelShowCancel();
        entity.setId(id);
        entity.setCancelDate(cancelDate);
        entity.setShowTime(showTime);
        entity.setReason(reason);
        entity.setActorName(actorName);
        entity.setNotes(notes);

        repository.update(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 데이터입니다: " + id));
        repository.deleteById(id);
    }
}
