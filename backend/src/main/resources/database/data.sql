CREATE TABLE IF NOT EXISTS customer (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100)        NOT NULL,
    last_name  VARCHAR(100)        NOT NULL,
    phone      VARCHAR(20),
    email      VARCHAR(255)        NOT NULL,
    created_at TIMESTAMP           NOT NULL,
    modified_at TIMESTAMP          NOT NULL
);

ALTER TABLE customer ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
