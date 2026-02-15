package com.schedulemng.repository;

import com.schedulemng.entity.DailyRemarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DailyRemarksRepository extends JpaRepository<DailyRemarks, Long> {
    
    // 특정 날짜의 특이사항 조회
    Optional<DailyRemarks> findByDate(String date);
    
    // 특정 월의 모든 특이사항 조회
    @Query("SELECT d FROM DailyRemarks d WHERE d.date LIKE :yearMonth%")
    List<DailyRemarks> findByYearMonth(@Param("yearMonth") String yearMonth);
} 