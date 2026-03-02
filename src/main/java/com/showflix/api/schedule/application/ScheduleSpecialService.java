package com.showflix.api.schedule.application;

import com.showflix.api.schedule.domain.ScheduleSpecial;
import com.showflix.api.schedule.domain.ScheduleSpecialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Layer - 특수예약 서비스
 */
@Service
public class ScheduleSpecialService {

    private final ScheduleSpecialRepository scheduleSpecialRepository;

    public ScheduleSpecialService(ScheduleSpecialRepository scheduleSpecialRepository) {
        this.scheduleSpecialRepository = scheduleSpecialRepository;
    }

    @Transactional(readOnly = true)
    public List<ScheduleSpecial> getAll() {
        return scheduleSpecialRepository.findAll();
    }

    @Transactional
    public void create(String reservationDate, String reservationTime,
                       String customerName, Integer peopleCount,
                       String contactInfo, String notes) {
        if (reservationDate == null || reservationDate.isBlank()) {
            throw new IllegalArgumentException("예약 날짜를 입력해주세요.");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("예약자명을 입력해주세요.");
        }

        ScheduleSpecial special = new ScheduleSpecial();
        special.setReservationDate(reservationDate);
        special.setReservationTime(reservationTime);
        special.setCustomerName(customerName);
        special.setPeopleCount(peopleCount);
        special.setContactInfo(contactInfo);
        special.setNotes(notes);

        scheduleSpecialRepository.insert(special);
    }

    @Transactional
    public void update(Long id, String reservationDate, String reservationTime,
                       String customerName, Integer peopleCount,
                       String contactInfo, String notes) {
        if (reservationDate == null || reservationDate.isBlank()) {
            throw new IllegalArgumentException("예약 날짜를 입력해주세요.");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("예약자명을 입력해주세요.");
        }

        ScheduleSpecial special = scheduleSpecialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다: " + id));

        special.setReservationDate(reservationDate);
        special.setReservationTime(reservationTime);
        special.setCustomerName(customerName);
        special.setPeopleCount(peopleCount);
        special.setContactInfo(contactInfo);
        special.setNotes(notes);

        scheduleSpecialRepository.update(special);
    }

    @Transactional
    public void delete(Long id) {
        scheduleSpecialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다: " + id));

        scheduleSpecialRepository.deleteById(id);
    }
}
