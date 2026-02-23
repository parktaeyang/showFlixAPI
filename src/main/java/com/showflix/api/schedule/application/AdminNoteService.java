package com.showflix.api.schedule.application;

import com.showflix.api.schedule.domain.AdminNote;
import com.showflix.api.schedule.domain.AdminNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Layer - 관리자 공지사항 유스케이스 서비스
 */
@Service
public class AdminNoteService {

    private static final String GLOBAL_ID = "GLOBAL";

    private final AdminNoteRepository adminNoteRepository;

    public AdminNoteService(AdminNoteRepository adminNoteRepository) {
        this.adminNoteRepository = adminNoteRepository;
    }

    /**
     * 공지사항 저장 (관리자 전용)
     */
    @Transactional
    public void saveAdminNote(String content, String updatedBy) {
        AdminNote adminNote = new AdminNote();
        adminNote.setId(GLOBAL_ID);
        adminNote.setContent(content);
        adminNote.setUpdatedBy(updatedBy);
        adminNoteRepository.save(adminNote);
    }

    /**
     * 공지사항 조회
     */
    @Transactional(readOnly = true)
    public AdminNote getAdminNote() {
        return adminNoteRepository.findById(GLOBAL_ID);
    }
}
