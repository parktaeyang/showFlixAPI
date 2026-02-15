package com.schedulemng.repository;

import com.schedulemng.entity.ScheduleTimeSlot;
import com.schedulemng.entity.ScheduleTimeSlotId; // 복합 키 클래스 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// JpaRepository의 제네릭 타입을 ScheduleTimeSlot (엔티티)과 ScheduleTimeSlotId (기본 키 타입)로 지정
public interface ScheduleTimeSlotRepository extends JpaRepository<ScheduleTimeSlot, ScheduleTimeSlotId> {

    // 특정 날짜의 모든 시간표 데이터를 시간 순서대로 조회하는 쿼리 메서드
    // 복합 키 내부 필드 (id.scheduleDate, id.timeSlot)에 접근하기 위해 'Id' 접두사를 사용.
    List<ScheduleTimeSlot> findByIdScheduleDateOrderByIdTimeSlotAsc(String scheduleDate);

    // 특정 날짜의 모든 시간표 확정 상태를 변경하는 쿼리 메서드
    @Modifying
    @Transactional
    @Query(value = "update schedule_time_slot set confirmed = :confirmed where schedule_date = :date", nativeQuery = true)
    int updateConfirmationByDate(@Param("date") String date, @Param("confirmed") String confirmed);

    // 참고: 특정 복합 키로 단일 엔티티를 조회할 때는 JpaRepository.findById(ScheduleTimeSlotId id) 메서드를 직접 사용가능.
    // 따라서 findByIdScheduleDateAndIdTimeSlot 같은 쿼리 메서드를 별도로 정의할 필요는 없음, JPA뭔가 ㅈㄴ어렵다.
    // TODO: 주석은 JPA 좀 잘 치게되면 나중에 정리...
}