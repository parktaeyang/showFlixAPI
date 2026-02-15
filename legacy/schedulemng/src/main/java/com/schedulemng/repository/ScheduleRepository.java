package com.schedulemng.repository;

import com.schedulemng.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    // 특정 월의 모든 스케줄 조회
    @Query("SELECT s FROM Schedule s WHERE YEAR(s.date) = :year AND MONTH(s.date) = :month ORDER BY s.date, s.username")
    List<Schedule> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
    
    // 특정 날짜의 모든 스케줄 조회
    List<Schedule> findByDateOrderByUsername(LocalDate date);
    
    // 특정 배우의 특정 월 스케줄 조회
    @Query("SELECT s FROM Schedule s WHERE s.username = :username AND YEAR(s.date) = :year AND MONTH(s.date) = :month ORDER BY s.date")
    List<Schedule> findByUsernameAndYearAndMonth(@Param("username") String username, @Param("year") int year, @Param("month") int month);
    
    // 특정 날짜와 배우의 스케줄 조회
    Optional<Schedule> findByDateAndUsername(LocalDate date, String username);
    
    // 특정 날짜의 스케줄 삭제
    void deleteByDateAndUsername(LocalDate date, String actorName);
    
    // 특정 월의 모든 배우 이름 조회
    @Query("SELECT DISTINCT s.username FROM Schedule s WHERE YEAR(s.date) = :year AND MONTH(s.date) = :month ORDER BY s.username")
    List<String> findDistinctActorNamesByYearAndMonth(@Param("year") int year, @Param("month") int month);
    
    // 특정 배우의 월간 총 시간 조회
    @Query("SELECT SUM(s.hours) FROM Schedule s WHERE s.username = :username AND YEAR(s.date) = :year AND MONTH(s.date) = :month")
    Double findTotalHoursByUsernameAndYearAndMonth(@Param("username") String username, @Param("year") int year, @Param("month") int month);
    
    // 특정 날짜의 총 시간 조회
    @Query("SELECT SUM(s.hours) FROM Schedule s WHERE s.date = :date")
    Double findTotalHoursByDate(@Param("date") LocalDate date);
}
