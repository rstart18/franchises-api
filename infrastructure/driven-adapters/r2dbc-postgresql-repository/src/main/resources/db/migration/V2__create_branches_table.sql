CREATE TABLE IF NOT EXISTS branches (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    franchise_id BIGINT NOT NULL REFERENCES franchises(id),
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    deleted_at   TIMESTAMP
);
