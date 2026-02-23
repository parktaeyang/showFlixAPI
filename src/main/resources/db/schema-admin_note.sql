CREATE TABLE IF NOT EXISTS admin_note (
    id          VARCHAR(50)   NOT NULL,
    content     TEXT          NULL,
    updated_by  VARCHAR(50)   NULL,
    updated_at  DATETIME      NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
