-- ============================================
-- 레거시 테이블 -> sf_ 테이블 데이터 이관
-- sf_create_tables.sql 실행 후 사용
-- 실행 전 반드시 DB 백업할 것!
-- ============================================

-- 1. sf_users <- schedule_users
-- 주의: 레거시 컬럼명이 JPA에 의해 camelCase일 수 있음
-- 실행 전 SHOW COLUMNS FROM schedule_users; 로 실제 컬럼명 확인 후 조정
INSERT INTO sf_users (userid, username, password, phone_number, account_type, role, is_admin, created_at)
SELECT
    userid,
    username,
    password,
    phone_number,
    COALESCE(account_type, 'ACTOR'),
    role,
    COALESCE(is_admin, 0),
    COALESCE(created_at, NOW())
FROM schedule_users
ON DUPLICATE KEY UPDATE username = VALUES(username);
commit;

-- 2. sf_selected_date <- selected_date
INSERT INTO sf_selected_date (date, user_id, user_name, open_hope, role, confirmed, remarks, created_at)
SELECT
    date, user_id, user_name, open_hope, role, nvl(confirmed, 'N'), remarks,
    NOW()
FROM selected_date
ON DUPLICATE KEY UPDATE user_name = VALUES(user_name);
commit;

-- 3. sf_schedule <- schedule
INSERT INTO sf_schedule (id, date, username, hours, memo, remarks, created_at)
SELECT
    id, date, username, hours, memo, remarks,
    NOW()
FROM schedule
ON DUPLICATE KEY UPDATE hours = VALUES(hours);
commit;

-- 4. sf_time_slot <- schedule_time_slot
INSERT INTO sf_time_slot (schedule_date, time_slot, theme, performer, confirmed, created_at)
SELECT
    schedule_date, time_slot, theme, performer, confirmed,
    NOW()
FROM schedule_time_slot
ON DUPLICATE KEY UPDATE theme = VALUES(theme);
commit;

-- 5. sf_special <- schedule_special (미사용 컬럼 제외)
INSERT INTO sf_special (id, reservation_date, reservation_time, customer_name, people_count, contact_info, notes, created_at)
SELECT
    id, reservation_date, reservation_time, customer_name, people_count, contact_info, notes,
    NOW()
FROM schedule_special
ON DUPLICATE KEY UPDATE customer_name = VALUES(customer_name);
commit;

-- 6. sf_summary <- schedule_summary
INSERT INTO sf_summary (id, user_id, date, hours, remarks, created_at)
SELECT
    id, user_id, date, hours, remarks,
    NOW()
FROM schedule_summary
ON DUPLICATE KEY UPDATE hours = VALUES(hours);
commit;

-- 7. sf_work_diary <- work_diary (또는 work_log)
-- 레거시 테이블명이 work_log인 경우 아래 FROM을 work_log로 변경
INSERT INTO sf_work_diary (id, date, manager, cash_payment, reservations, event, store_related, notes, created_at)
SELECT
    id, date, manager, cash_payment, reservations, event, store_related, notes,
    NOW()
FROM work_diary
ON DUPLICATE KEY UPDATE manager = VALUES(manager);
commit;
select * from work_diary;

-- 8. sf_voucher_tip <- actor_voucher_tip
INSERT INTO sf_voucher_tip (id, date, user_id, user_name, voucher, tip, created_at)
SELECT
    id, date, user_id, user_name, voucher, tip,
    NOW()
FROM actor_voucher_tip
ON DUPLICATE KEY UPDATE voucher = VALUES(voucher);
commit;

-- 9. sf_admin_note <- admin_note
INSERT INTO sf_admin_note (id, content, updated_by, updated_at)
SELECT id, content, updated_by, updated_at
FROM admin_note
ON DUPLICATE KEY UPDATE content = VALUES(content);
COMMIT ;

-- ============================================
-- 이관 결과 검증 쿼리 (건수 비교)
-- ============================================
SELECT 'schedule_users' AS tbl, COUNT(*) AS cnt FROM schedule_users
UNION ALL SELECT 'sf_users', COUNT(*) FROM sf_users
UNION ALL SELECT 'selected_date', COUNT(*) FROM selected_date
UNION ALL SELECT 'sf_selected_date', COUNT(*) FROM sf_selected_date
UNION ALL SELECT 'schedule', COUNT(*) FROM schedule
UNION ALL SELECT 'sf_schedule', COUNT(*) FROM sf_schedule
UNION ALL SELECT 'schedule_time_slot', COUNT(*) FROM schedule_time_slot
UNION ALL SELECT 'sf_time_slot', COUNT(*) FROM sf_time_slot
UNION ALL SELECT 'schedule_special', COUNT(*) FROM schedule_special
UNION ALL SELECT 'sf_special', COUNT(*) FROM sf_special
UNION ALL SELECT 'schedule_summary', COUNT(*) FROM schedule_summary
UNION ALL SELECT 'sf_summary', COUNT(*) FROM sf_summary
UNION ALL SELECT 'work_diary', COUNT(*) FROM work_diary
UNION ALL SELECT 'sf_work_diary', COUNT(*) FROM sf_work_diary
UNION ALL SELECT 'actor_voucher_tip', COUNT(*) FROM actor_voucher_tip
UNION ALL SELECT 'sf_voucher_tip', COUNT(*) FROM sf_voucher_tip
UNION ALL SELECT 'admin_note', COUNT(*) FROM admin_note
UNION ALL SELECT 'sf_admin_note', COUNT(*) FROM sf_admin_note;
