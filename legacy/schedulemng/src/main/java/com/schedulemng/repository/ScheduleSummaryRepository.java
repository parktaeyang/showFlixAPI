package com.schedulemng.repository;

import com.schedulemng.entity.ScheduleSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleSummaryRepository extends JpaRepository<ScheduleSummary, Long> {
    
    // 특정 사용자의 특정 날짜 스케줄 조회
    Optional<ScheduleSummary> findByUserIdAndDate(String userId, String date);
    
    // 특정 월의 모든 스케줄 조회
    @Query("SELECT s FROM ScheduleSummary s WHERE s.date LIKE :yearMonth%")
    List<ScheduleSummary> findByYearMonth(@Param("yearMonth") String yearMonth);
    
    // 특정 사용자의 특정 월 스케줄 조회
    @Query("SELECT s FROM ScheduleSummary s WHERE s.userId = :userId AND s.date LIKE :yearMonth%")
    List<ScheduleSummary> findByUserIdAndYearMonth(@Param("userId") String userId, @Param("yearMonth") String yearMonth);
    
    // 특정 날짜의 모든 스케줄 조회
    List<ScheduleSummary> findByDate(String date);
    
    // 특정 월의 특정 사용자 스케줄 삭제
    @Query("DELETE FROM ScheduleSummary s WHERE s.userId = :userId AND s.date LIKE :yearMonth%")
    void deleteByUserIdAndYearMonth(@Param("userId") String userId, @Param("yearMonth") String yearMonth);
} 