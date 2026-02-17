-- selected_date 테이블 (MySQL)
-- 기존 프로젝트와 호환되는 스키마

CREATE TABLE IF NOT EXISTS selected_date (
    date        VARCHAR(10)   NOT NULL,
    user_id     VARCHAR(50)   NOT NULL,
    user_name   VARCHAR(100)  NULL,
    open_hope   TINYINT(1)    DEFAULT 0,
    role        VARCHAR(20)   NULL,
    confirmed   CHAR(1)       DEFAULT 'N',
    remarks     TEXT          NULL,
    PRIMARY KEY (date, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
