-- ============================================
-- 레거시 테이블 삭제
-- sf_ 테이블로 전환 완료 및 안정화 확인 후 실행
-- 실행 전 반드시 최종 백업할 것!
-- ============================================

DROP TABLE IF EXISTS schedule_users;
DROP TABLE IF EXISTS selected_date;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS schedule_time_slot;
DROP TABLE IF EXISTS schedule_special;
DROP TABLE IF EXISTS schedule_summary;
DROP TABLE IF EXISTS work_diary;
DROP TABLE IF EXISTS work_log;
DROP TABLE IF EXISTS actor_voucher_tip;
DROP TABLE IF EXISTS admin_note;
DROP TABLE IF EXISTS daily_remarks;
