CREATE TABLE sf_user_action_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    username     VARCHAR(100) NOT NULL                COMMENT '로그인 사용자 ID (sf_users.userid)',
    user_name    VARCHAR(100)                         COMMENT '사용자 표시명',
    action       VARCHAR(20)  NOT NULL                COMMENT '행동 유형: CREATE / UPDATE / DELETE / FAILED',
    target_table VARCHAR(100) NOT NULL                COMMENT '대상 테이블명 (e.g. sf_selected_date)',
    target_id    VARCHAR(200)                         COMMENT '대상 식별자 (id 또는 복합키 표현)',
    description  VARCHAR(500)                         COMMENT '사람이 읽을 수 있는 행동 설명',
    request_data TEXT                                 COMMENT '입력 데이터 (JSON)',
    created_at   DATETIME     NOT NULL DEFAULT NOW()  COMMENT '기록 시각',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자 행동 로그';

CREATE INDEX idx_ual_username   ON sf_user_action_log (username);
CREATE INDEX idx_ual_created_at ON sf_user_action_log (created_at);

select * from sf_user_action_log;