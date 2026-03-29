-- ============================================
-- ShowFlix 리뉴얼 테이블 (sf_ 접두사)
-- MySQL 8.0+ / MariaDB 10.5+ 호환
-- 레거시 테이블과 병행 운영 후 검증 완료 시 레거시 삭제
-- ============================================

-- 1. sf_users (기존: schedule_users)
CREATE TABLE IF NOT EXISTS sf_users (
    userid        VARCHAR(50)   NOT NULL COMMENT '사용자 ID (예: actor01, staff01)',
    username      VARCHAR(100)  NOT NULL COMMENT '이름',
    password      VARCHAR(255)  NOT NULL COMMENT '암호화된 비밀번호',
    phone_number  VARCHAR(20)   NULL     COMMENT '연락처',
    account_type  VARCHAR(20)   NOT NULL DEFAULT 'ACTOR' COMMENT '계정유형: ACTOR, STAFF, CAPTAIN, ADMIN',
    role          VARCHAR(20)   NULL     COMMENT '역할: DOOR, HOLEMAN, OPER, HELPER, KITCHEN, MALE1~3, FEMALE1~3',
    is_admin      TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '관리자 여부',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at    DATETIME      NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (userid),
    INDEX idx_sf_users_account_type (account_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자 정보';

-- 2. sf_selected_date (기존: selected_date)
CREATE TABLE IF NOT EXISTS sf_selected_date (
    date          VARCHAR(10)   NOT NULL COMMENT '날짜 (YYYY-MM-DD)',
    user_id       VARCHAR(50)   NOT NULL COMMENT '사용자 ID (FK: sf_users.userid)',
    user_name     VARCHAR(100)  NULL     COMMENT '사용자 이름 (비정규화)',
    open_hope     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '오픈 희망 여부',
    role          VARCHAR(20)   NULL     COMMENT '해당 날짜 역할',
    confirmed     CHAR(1)       NOT NULL DEFAULT 'N' COMMENT '확정 여부 (Y/N)',
    remarks       TEXT          NULL     COMMENT '비고',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at    DATETIME      NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (date, user_id),
    INDEX idx_sf_selected_date_user (user_id),
    INDEX idx_sf_selected_date_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='출근일 선택';

-- 3. sf_schedule (기존: schedule)
CREATE TABLE IF NOT EXISTS sf_schedule (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    date          VARCHAR(10)   NOT NULL COMMENT '날짜 (YYYY-MM-DD)',
    username      VARCHAR(100)  NOT NULL COMMENT '배우 이름',
    hours         DOUBLE        NOT NULL DEFAULT 0 COMMENT '근무시간',
    memo          TEXT          NULL     COMMENT '메모',
    remarks       TEXT          NULL     COMMENT '일별 특이사항',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at    DATETIME      NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sf_schedule_date_user (date, username),
    INDEX idx_sf_schedule_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='근무시간 기록';

-- 4. sf_time_slot (기존: schedule_time_slot)
CREATE TABLE IF NOT EXISTS sf_time_slot (
    schedule_date VARCHAR(10)   NOT NULL COMMENT '날짜 (YYYY-MM-DD)',
    time_slot     VARCHAR(5)    NOT NULL COMMENT '시간 (HH:mm)',
    theme         VARCHAR(100)  NULL     COMMENT '테마',
    performer     VARCHAR(500)  NULL     COMMENT '출연자 (콤마 구분)',
    confirmed     CHAR(1)       NOT NULL DEFAULT 'N' COMMENT '확정 여부 (Y/N)',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at    DATETIME      NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (schedule_date, time_slot),
    INDEX idx_sf_timeslot_date (schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='공연 시간표';

-- 5. sf_special (기존: schedule_special)
CREATE TABLE IF NOT EXISTS sf_special (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    reservation_date  VARCHAR(10)   NOT NULL COMMENT '예약 날짜 (YYYY-MM-DD)',
    reservation_time  VARCHAR(5)    NULL     COMMENT '예약 시간 (HH:mm)',
    customer_name     VARCHAR(100)  NULL     COMMENT '예약자명',
    people_count      INT           NULL     COMMENT '인원수',
    contact_info      VARCHAR(50)   NULL     COMMENT '연락처',
    notes             TEXT          NULL     COMMENT '비고',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at        DATETIME      NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    INDEX idx_sf_special_date (reservation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='특수예약';

-- 6. sf_summary (기존: schedule_summary)
CREATE TABLE IF NOT EXISTS sf_summary (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    user_id       VARCHAR(50)   NOT NULL COMMENT '사용자 ID',
    date          VARCHAR(10)   NOT NULL COMMENT '날짜 (YYYY-MM-DD)',
    hours         VARCHAR(10)   NOT NULL DEFAULT '0' COMMENT '근무시간',
    remarks       TEXT          NULL     COMMENT '비고',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at    DATETIME      NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sf_summary_user_date (user_id, date),
    INDEX idx_sf_summary_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='출근시간 요약';

-- 7. sf_work_diary (기존: work_diary / work_log)
CREATE TABLE IF NOT EXISTS sf_work_diary (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    date            VARCHAR(10)   NOT NULL COMMENT '날짜 (YYYY-MM-DD)',
    manager         VARCHAR(100)  NOT NULL COMMENT '담당자',
    cash_payment    TEXT          NULL     COMMENT '현금 결제',
    reservations    TEXT          NULL     COMMENT '지정석/특수예약/멤버십',
    event           TEXT          NULL     COMMENT '이벤트',
    store_related   TEXT          NULL     COMMENT '가게 관련',
    notes           TEXT          NULL     COMMENT '특이사항',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      DATETIME      NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sf_diary_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='업무일지';

-- 8. sf_voucher_tip (기존: actor_voucher_tip)
CREATE TABLE IF NOT EXISTS sf_voucher_tip (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    date          VARCHAR(10)   NOT NULL COMMENT '날짜 (YYYY-MM-DD)',
    user_id       VARCHAR(50)   NOT NULL COMMENT '사용자 ID',
    user_name     VARCHAR(100)  NULL     COMMENT '사용자 이름 (비정규화)',
    voucher       INT           NOT NULL DEFAULT 0 COMMENT '바우처 수',
    tip           INT           NOT NULL DEFAULT 0 COMMENT '팁 금액',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at    DATETIME      NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sf_voucher_date_user (date, user_id),
    INDEX idx_sf_voucher_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='배우 바우처/팁';

-- 9. sf_admin_note (기존: admin_note)
CREATE TABLE IF NOT EXISTS sf_admin_note (
    id            VARCHAR(50)   NOT NULL COMMENT '메모 ID',
    content       TEXT          NULL     COMMENT '내용',
    updated_by    VARCHAR(50)   NULL     COMMENT '수정자',
    updated_at    DATETIME      NULL     COMMENT '수정일시',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='관리자 메모';
