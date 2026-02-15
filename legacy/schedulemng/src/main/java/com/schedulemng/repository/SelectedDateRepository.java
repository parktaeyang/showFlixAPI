package com.schedulemng.repository;

import com.schedulemng.entity.SelectedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SelectedDateRepository extends JpaRepository<SelectedDate, Long> {

    List<SelectedDate> findByDateBetween(@Param("start") String start,
                                         @Param("end") String end);

    @Modifying
    @Transactional
    @Query(value = """
                    update selected_date
                    set role = :role, remarks = coalesce(:remarks, remarks)
                    where date = :date
                    and user_id = :userId
                    """
            , nativeQuery = true)
    int updateRoleByDateAndUserId(@Param("date") String date,
                                  @Param("userId") String userId,
                                  @Param("role") String role,
                                  @Param("remarks") String remarks);

    @Modifying
    @Transactional
    @Query("""
            delete from SelectedDate s
            where s.date = :date
            and s.userId = :userId
            """
    )
    int deleteByDateAndUserId(@Param("date") String date,
                              @Param("userId") String userId);

    @Modifying
    @Transactional
    @Query(value = """
                    update selected_date
                    set confirmed = :confirmed
                    where date = :date
                    """
            , nativeQuery = true)
    int updateConfirmationByDate(@Param("date") String date, @Param("confirmed") String confirmed);

}