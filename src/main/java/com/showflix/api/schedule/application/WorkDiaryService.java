package com.showflix.api.schedule.application;

import com.showflix.api.schedule.domain.WorkDiary;
import com.showflix.api.schedule.domain.WorkDiaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Layer - 업무일지 서비스
 */
@Service
public class WorkDiaryService {

    private final WorkDiaryRepository repository;

    public WorkDiaryService(WorkDiaryRepository repository) {
        this.repository = repository;
    }

    /**
     * 월별 업무일지 조회
     */
    @Transactional(readOnly = true)
    public List<WorkDiary> getByMonth(int year, int month) {
        String yearMonth = String.format("%04d-%02d", year, month);
        return repository.findByMonth(yearMonth);
    }

    /**
     * 업무일지 신규 등록
     */
    @Transactional
    public WorkDiary create(WorkDiaryItem item) {
        WorkDiary diary = new WorkDiary();
        diary.setDate(item.date());
        diary.setManager(item.manager());
        diary.setCashPayment(item.cashPayment());
        diary.setReservations(item.reservations());
        diary.setEvent(item.event());
        diary.setStoreRelated(item.storeRelated());
        diary.setNotes(item.notes());
        repository.save(diary);
        return diary;
    }

    /**
     * 업무일지 수정
     */
    @Transactional
    public WorkDiary update(Long id, WorkDiaryItem item) {
        WorkDiary diary = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 업무일지를 찾을 수 없습니다. id=" + id));
        diary.setDate(item.date());
        diary.setManager(item.manager());
        diary.setCashPayment(item.cashPayment());
        diary.setReservations(item.reservations());
        diary.setEvent(item.event());
        diary.setStoreRelated(item.storeRelated());
        diary.setNotes(item.notes());
        repository.update(diary);
        return diary;
    }

    /**
     * 업무일지 삭제
     */
    @Transactional
    public void delete(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 업무일지를 찾을 수 없습니다. id=" + id));
        repository.deleteById(id);
    }

    /**
     * 저장/수정 요청 DTO
     */
    public record WorkDiaryItem(
            String date,
            String manager,
            String cashPayment,
            String reservations,
            String event,
            String storeRelated,
            String notes
    ) {}
}
