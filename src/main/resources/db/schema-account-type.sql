-- schedule_users 테이블에 계정유형, 역할 컬럼 추가
ALTER TABLE schedule_users
    ADD COLUMN IF NOT EXISTS account_type VARCHAR(20) NULL,
    ADD COLUMN IF NOT EXISTS role         VARCHAR(20) NULL;
