package com.showflix.api.schedule.domain;

/**
 * Domain Layer - 관리자 공지사항 저장소 인터페이스
 */
public interface AdminNoteRepository {

    /**
     * ID로 공지사항 조회
     */
    AdminNote findById(String id);

    /**
     * 공지사항 저장 (INSERT or UPDATE)
     */
    void save(AdminNote adminNote);
}
