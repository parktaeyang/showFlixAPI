package com.schedulemng.service;

import com.schedulemng.dto.ScheduleSpecialRequestDto;
import com.schedulemng.dto.ScheduleSpecialResponseDto;
import com.schedulemng.entity.ScheduleSpecial;
import com.schedulemng.repository.ScheduleSpecialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleSpecialService {
    
    private final ScheduleSpecialRepository scheduleSpecialRepository;
    
    /**
     * 모든 특수 예약 조회
     */
    public List<ScheduleSpecialResponseDto> getAllSpecialReservations() {
        List<ScheduleSpecial> reservations = scheduleSpecialRepository.findAll(
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return reservations.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 페이징된 특수 예약 조회
     */
    public ScheduleSpecialResponseDto.PagedResponse getSpecialReservationsWithPaging(
            int page, int size, String sortBy, String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(Sort.Direction.DESC, sortBy) : 
            Sort.by(Sort.Direction.ASC, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ScheduleSpecial> reservationPage = scheduleSpecialRepository.findAll(pageable);
        
        return ScheduleSpecialResponseDto.PagedResponse.builder()
                .content(reservationPage.getContent().stream()
                        .map(this::convertToResponseDto)
                        .collect(Collectors.toList()))
                .page(reservationPage.getNumber())
                .size(reservationPage.getSize())
                .totalElements(reservationPage.getTotalElements())
                .totalPages(reservationPage.getTotalPages())
                .first(reservationPage.isFirst())
                .last(reservationPage.isLast())
                .hasNext(reservationPage.hasNext())
                .hasPrevious(reservationPage.hasPrevious())
                .build();
    }
    
    /**
     * ID로 특수 예약 조회
     */
    public Optional<ScheduleSpecialResponseDto> getSpecialReservationById(Long id) {
        return scheduleSpecialRepository.findById(id)
                .map(this::convertToResponseDto);
    }
    
    /**
     * 특수 예약 생성
     */
    @Transactional
    public ScheduleSpecialResponseDto createSpecialReservation(ScheduleSpecialRequestDto requestDto) {
        log.info("Creating new special reservation: {}", requestDto);
        
        ScheduleSpecial entity = convertToEntity(requestDto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        // 예상 수익 자동 계산 (인원수 * 5만원)
        if (entity.getExpectedRevenue() == null && entity.getPeopleCount() != null) {
            entity.setExpectedRevenue((long) (entity.getPeopleCount() * 50000));
        }
        
        ScheduleSpecial savedEntity = scheduleSpecialRepository.save(entity);
        log.info("Special reservation created with ID: {}", savedEntity.getId());
        
        return convertToResponseDto(savedEntity);
    }
    
    /**
     * 특수 예약 수정
     */
    @Transactional
    public Optional<ScheduleSpecialResponseDto> updateSpecialReservation(
            Long id, ScheduleSpecialRequestDto requestDto) {
        
        log.info("Updating special reservation with ID: {}", id);
        
        return scheduleSpecialRepository.findById(id)
                .map(existingEntity -> {
                    updateEntityFromRequest(existingEntity, requestDto);
                    existingEntity.setUpdatedAt(LocalDateTime.now());
                    
                    // 예상 수익 자동 계산
                    if (existingEntity.getExpectedRevenue() == null && existingEntity.getPeopleCount() != null) {
                        existingEntity.setExpectedRevenue((long) (existingEntity.getPeopleCount() * 50000));
                    }
                    
                    ScheduleSpecial savedEntity = scheduleSpecialRepository.save(existingEntity);
                    log.info("Special reservation updated with ID: {}", savedEntity.getId());
                    
                    return convertToResponseDto(savedEntity);
                });
    }
    
    /**
     * 특수 예약 삭제
     */
    @Transactional
    public boolean deleteSpecialReservation(Long id) {
        log.info("Deleting special reservation with ID: {}", id);
        
        if (scheduleSpecialRepository.existsById(id)) {
            scheduleSpecialRepository.deleteById(id);
            log.info("Special reservation deleted with ID: {}", id);
            return true;
        }
        return false;
    }
    
    /**
     * 예약 상태별 조회
     */
    public List<ScheduleSpecialResponseDto> getSpecialReservationsByStatus(String status) {
        try {
            ScheduleSpecial.ReservationStatus reservationStatus = 
                ScheduleSpecial.ReservationStatus.valueOf(status.toUpperCase());
            
            List<ScheduleSpecial> reservations = scheduleSpecialRepository.findByReservationStatus(reservationStatus);
            return reservations.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid reservation status: {}", status);
            return List.of();
        }
    }
    
    /**
     * 검색 기능
     */
    public List<ScheduleSpecialResponseDto> searchSpecialReservations(
            String customerName, String contactInfo, String specialRemarks, String status) {
        
        ScheduleSpecial.ReservationStatus reservationStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                reservationStatus = ScheduleSpecial.ReservationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid reservation status: {}", status);
            }
        }
        
        Pageable pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ScheduleSpecial> searchResults = scheduleSpecialRepository.findBySearchCriteria(
                customerName, contactInfo, specialRemarks, reservationStatus, pageable);
        
        return searchResults.getContent().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 통계 정보 조회
     */
    public ScheduleSpecialResponseDto.StatisticsResponse getStatistics() {
        long totalReservations = scheduleSpecialRepository.count();
        long pendingReservations = scheduleSpecialRepository.countByReservationStatus(
            ScheduleSpecial.ReservationStatus.PENDING);
        long confirmedReservations = scheduleSpecialRepository.countByReservationStatus(
            ScheduleSpecial.ReservationStatus.CONFIRMED);
        long completedReservations = scheduleSpecialRepository.countByReservationStatus(
            ScheduleSpecial.ReservationStatus.COMPLETED);
        long cancelledReservations = scheduleSpecialRepository.countByReservationStatus(
            ScheduleSpecial.ReservationStatus.CANCELLED);
        
        long totalExpectedRevenue = scheduleSpecialRepository.sumTotalExpectedRevenue();
        long pendingRevenue = scheduleSpecialRepository.sumExpectedRevenueByStatus(
            ScheduleSpecial.ReservationStatus.PENDING);
        long confirmedRevenue = scheduleSpecialRepository.sumExpectedRevenueByStatus(
            ScheduleSpecial.ReservationStatus.CONFIRMED);
        long completedRevenue = scheduleSpecialRepository.sumExpectedRevenueByStatus(
            ScheduleSpecial.ReservationStatus.COMPLETED);
        
        return ScheduleSpecialResponseDto.StatisticsResponse.builder()
                .totalReservations(totalReservations)
                .pendingReservations(pendingReservations)
                .confirmedReservations(confirmedReservations)
                .completedReservations(completedReservations)
                .cancelledReservations(cancelledReservations)
                .totalExpectedRevenue(totalExpectedRevenue)
                .pendingRevenue(pendingRevenue)
                .confirmedRevenue(confirmedRevenue)
                .completedRevenue(completedRevenue)
                .build();
    }
    
    /**
     * 하이라이트 타입 변경
     */
    @Transactional
    public Optional<ScheduleSpecialResponseDto> updateHighlightType(Long id, String highlightType) {
        return scheduleSpecialRepository.findById(id)
                .map(entity -> {
                    try {
                        ScheduleSpecial.HighlightType newHighlightType = 
                            ScheduleSpecial.HighlightType.valueOf(highlightType.toUpperCase());
                        entity.setHighlightType(newHighlightType);
                        entity.setUpdatedAt(LocalDateTime.now());
                        
                        ScheduleSpecial savedEntity = scheduleSpecialRepository.save(entity);
                        return convertToResponseDto(savedEntity);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid highlight type: {}", highlightType);
                        return convertToResponseDto(entity);
                    }
                });
    }
    
    /**
     * 예약 상태 변경
     */
    @Transactional
    public Optional<ScheduleSpecialResponseDto> updateReservationStatus(Long id, String status) {
        return scheduleSpecialRepository.findById(id)
                .map(entity -> {
                    try {
                        ScheduleSpecial.ReservationStatus newStatus = 
                            ScheduleSpecial.ReservationStatus.valueOf(status.toUpperCase());
                        entity.setReservationStatus(newStatus);
                        entity.setUpdatedAt(LocalDateTime.now());
                        
                        ScheduleSpecial savedEntity = scheduleSpecialRepository.save(entity);
                        return convertToResponseDto(savedEntity);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid reservation status: {}", status);
                        return convertToResponseDto(entity);
                    }
                });
    }
    
    // DTO 변환 메서드들
    private ScheduleSpecialResponseDto convertToResponseDto(ScheduleSpecial entity) {
        return ScheduleSpecialResponseDto.builder()
                .id(entity.getId())
                .reservationDate(entity.getReservationDate())
                .reservationTime(entity.getReservationTime())
                .specialRemarks(entity.getSpecialRemarks())
                .customerName(entity.getCustomerName())
                .peopleCount(entity.getPeopleCount())
                .paymentStatus(entity.getPaymentStatus())
                .contactInfo(entity.getContactInfo())
                .notes(entity.getNotes())
                .reservationStatus(entity.getReservationStatus() != null ? 
                    entity.getReservationStatus().name() : null)
                .highlightType(entity.getHighlightType() != null ? 
                    entity.getHighlightType().name() : null)
                .expectedRevenue(entity.getExpectedRevenue())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
    
    private ScheduleSpecial convertToEntity(ScheduleSpecialRequestDto requestDto) {
        return ScheduleSpecial.builder()
                .reservationDate(requestDto.getReservationDate())
                .reservationTime(requestDto.getReservationTime())
                .specialRemarks(requestDto.getSpecialRemarks())
                .customerName(requestDto.getCustomerName())
                .peopleCount(requestDto.getPeopleCount())
                .paymentStatus(requestDto.getPaymentStatus())
                .contactInfo(requestDto.getContactInfo())
                .notes(requestDto.getNotes())
                .reservationStatus(requestDto.getReservationStatus() != null ? 
                    ScheduleSpecial.ReservationStatus.valueOf(requestDto.getReservationStatus().toUpperCase()) : 
                    ScheduleSpecial.ReservationStatus.PENDING)
                .highlightType(requestDto.getHighlightType() != null ? 
                    ScheduleSpecial.HighlightType.valueOf(requestDto.getHighlightType().toUpperCase()) : 
                    ScheduleSpecial.HighlightType.NONE)
                .expectedRevenue(requestDto.getExpectedRevenue())
                .createdBy(requestDto.getCreatedBy())
                .updatedBy(requestDto.getUpdatedBy())
                .build();
    }
    
    private void updateEntityFromRequest(ScheduleSpecial entity, ScheduleSpecialRequestDto requestDto) {
        if (requestDto.getReservationDate() != null) {
            entity.setReservationDate(requestDto.getReservationDate());
        }
        if (requestDto.getReservationTime() != null) {
            entity.setReservationTime(requestDto.getReservationTime());
        }
        if (requestDto.getSpecialRemarks() != null) {
            entity.setSpecialRemarks(requestDto.getSpecialRemarks());
        }
        if (requestDto.getCustomerName() != null) {
            entity.setCustomerName(requestDto.getCustomerName());
        }
        if (requestDto.getPeopleCount() != null) {
            entity.setPeopleCount(requestDto.getPeopleCount());
        }
        if (requestDto.getPaymentStatus() != null) {
            entity.setPaymentStatus(requestDto.getPaymentStatus());
        }
        if (requestDto.getContactInfo() != null) {
            entity.setContactInfo(requestDto.getContactInfo());
        }
        if (requestDto.getNotes() != null) {
            entity.setNotes(requestDto.getNotes());
        }
        if (requestDto.getReservationStatus() != null) {
            try {
                entity.setReservationStatus(
                    ScheduleSpecial.ReservationStatus.valueOf(requestDto.getReservationStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid reservation status: {}", requestDto.getReservationStatus());
            }
        }
        if (requestDto.getHighlightType() != null) {
            try {
                entity.setHighlightType(
                    ScheduleSpecial.HighlightType.valueOf(requestDto.getHighlightType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid highlight type: {}", requestDto.getHighlightType());
            }
        }
        if (requestDto.getExpectedRevenue() != null) {
            entity.setExpectedRevenue(requestDto.getExpectedRevenue());
        }
        if (requestDto.getUpdatedBy() != null) {
            entity.setUpdatedBy(requestDto.getUpdatedBy());
        }
    }
}
