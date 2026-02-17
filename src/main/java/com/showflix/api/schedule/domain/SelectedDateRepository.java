package com.showflix.api.schedule.domain;

import java.util.List;

/**
 * Domain Layer - SelectedDate 저장소 Port
 */
public interface SelectedDateRepository {

    List<SelectedDate> findByDateBetween(String start, String end);

    void saveAll(List<SelectedDate> list);

    int updateRoleByDateAndUserId(String date, String userId, String role, String remarks);

    int deleteByDateAndUserId(String date, String userId);

    int updateConfirmationByDate(String date, String confirmed);
}
