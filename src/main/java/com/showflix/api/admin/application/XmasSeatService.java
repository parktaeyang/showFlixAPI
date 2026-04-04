package com.showflix.api.admin.application;

import com.showflix.api.admin.domain.XmasSeat;
import com.showflix.api.admin.domain.XmasSeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class XmasSeatService {

    private final XmasSeatRepository repository;

    public XmasSeatService(XmasSeatRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<XmasSeat> getByDate(String eventDate) {
        return repository.findByEventDate(eventDate);
    }

    @Transactional
    public void create(String eventDate, String seatLabel, String customerName,
                       String phone, Integer peopleCount, String notes) {
        if (eventDate == null || eventDate.isBlank()) {
            throw new IllegalArgumentException("이벤트 날짜를 입력해주세요.");
        }

        XmasSeat entity = new XmasSeat();
        entity.setEventDate(eventDate);
        entity.setSeatLabel(seatLabel);
        entity.setCustomerName(customerName);
        entity.setPhone(phone);
        entity.setPeopleCount(peopleCount);
        entity.setNotes(notes);

        repository.insert(entity);
    }

    @Transactional
    public void update(Long id, String eventDate, String seatLabel, String customerName,
                       String phone, Integer peopleCount, String notes) {
        if (eventDate == null || eventDate.isBlank()) {
            throw new IllegalArgumentException("이벤트 날짜를 입력해주세요.");
        }

        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 데이터입니다: " + id));

        XmasSeat entity = new XmasSeat();
        entity.setId(id);
        entity.setEventDate(eventDate);
        entity.setSeatLabel(seatLabel);
        entity.setCustomerName(customerName);
        entity.setPhone(phone);
        entity.setPeopleCount(peopleCount);
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
