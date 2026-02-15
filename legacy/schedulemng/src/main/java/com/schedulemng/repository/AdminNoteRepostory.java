package com.schedulemng.repository;

import com.schedulemng.entity.AdminNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface AdminNoteRepostory extends JpaRepository<AdminNote, Long> {

    AdminNote findById(@Param("id") String id);
}
