package com.schedulemng.repository;

import com.schedulemng.entity.ScheduleSpecial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleSpecialRepository extends JpaRepository<ScheduleSpecial, Long> {
    
    /**
     * 예약 상태별 조회
     */
    List<ScheduleSpecial> findByReservationStatus(ScheduleSpecial.ReservationStatus status);
    
    /**
     * 예약 상태별 페이징 조회
     */
    Page<ScheduleSpecial> findByReservationStatus(ScheduleSpecial.ReservationStatus status, Pageable pageable);
    
    /**
     * 고객명으로 검색
     */
    List<ScheduleSpecial> findByCustomerNameContainingIgnoreCase(String customerName);
    
    /**
     * 연락처로 검색
     */
    List<ScheduleSpecial> findByContactInfoContaining(String contactInfo);
    
    /**
     * 특이사항으로 검색
     */
    List<ScheduleSpecial> findBySpecialRemarksContainingIgnoreCase(String specialRemarks);
    
    /**
     * 하이라이트 타입별 조회
     */
    List<ScheduleSpecial> findByHighlightType(ScheduleSpecial.HighlightType highlightType);
    
    /**
     * 날짜 범위로 조회
     */
    @Query("SELECT s FROM ScheduleSpecial s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<ScheduleSpecial> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * 인원수 범위로 조회
     */
    List<ScheduleSpecial> findByPeopleCountBetween(Integer minPeople, Integer maxPeople);
    
    /**
     * 예상 수익 합계 조회
     */
    @Query("SELECT COALESCE(SUM(s.expectedRevenue), 0) FROM ScheduleSpecial s WHERE s.reservationStatus = :status")
    Long sumExpectedRevenueByStatus(@Param("status") ScheduleSpecial.ReservationStatus status);
    
    /**
     * 전체 예상 수익 합계 조회
     */
    @Query("SELECT COALESCE(SUM(s.expectedRevenue), 0) FROM ScheduleSpecial s")
    Long sumTotalExpectedRevenue();
    
    /**
     * 상태별 예약 수 조회
     */
    long countByReservationStatus(ScheduleSpecial.ReservationStatus status);
    
    /**
     * 복합 검색 (고객명, 연락처, 특이사항)
     */
    @Query("SELECT s FROM ScheduleSpecial s WHERE " +
           "(:customerName IS NULL OR LOWER(s.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:contactInfo IS NULL OR s.contactInfo LIKE CONCAT('%', :contactInfo, '%')) AND " +
           "(:specialRemarks IS NULL OR LOWER(s.specialRemarks) LIKE LOWER(CONCAT('%', :specialRemarks, '%'))) AND " +
           "(:status IS NULL OR s.reservationStatus = :status)")
    Page<ScheduleSpecial> findBySearchCriteria(@Param("customerName") String customerName,
                                              @Param("contactInfo") String contactInfo,
                                              @Param("specialRemarks") String specialRemarks,
                                              @Param("status") ScheduleSpecial.ReservationStatus status,
                                              Pageable pageable);
    
    /**
     * 최근 생성된 예약 조회
     */
    List<ScheduleSpecial> findTop10ByOrderByCreatedAtDesc();
    
    /**
     * 특정 기간 내 예약 조회
     */
    @Query("SELECT s FROM ScheduleSpecial s WHERE s.createdAt >= :startDate ORDER BY s.createdAt DESC")
    List<ScheduleSpecial> findRecentReservations(@Param("startDate") LocalDateTime startDate);
}
