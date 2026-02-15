package com.schedulemng.repository;

import com.schedulemng.entity.WorkLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    
    // 특정 월의 모든 업무일지 조회
    @Query("SELECT w FROM WorkLog w WHERE w.date LIKE :yearMonth% ORDER BY w.date ASC")
    List<WorkLog> findByYearMonth(@Param("yearMonth") String yearMonth);

} 