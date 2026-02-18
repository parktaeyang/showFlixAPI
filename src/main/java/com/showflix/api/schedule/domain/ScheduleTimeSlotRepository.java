package com.showflix.api.schedule.domain;

import java.util.List;

/**
 * schedule_time_slot 리포지토리 포트
 */
public interface ScheduleTimeSlotRepository {

    /** 특정 날짜의 모든 시간표 조회 (시간순 정렬) */
    List<ScheduleTimeSlot> findByScheduleDate(String scheduleDate);

    /** 저장 또는 업데이트 (ON DUPLICATE KEY UPDATE) */
    void save(ScheduleTimeSlot slot);

    /** 특정 날짜의 시간표 전체 삭제 후 재삽입 */
    void deleteByScheduleDate(String scheduleDate);

    /** 특정 날짜의 confirmed 상태 일괄 변경 */
    void updateConfirmationByDate(String scheduleDate, String confirmed);
}
