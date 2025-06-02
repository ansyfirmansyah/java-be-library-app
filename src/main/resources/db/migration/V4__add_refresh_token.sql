CREATE TABLE refresh_token
(
    id         UUID PRIMARY KEY,
    user_id    UUID                NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP           NOT NULL,
    revoked    BOOLEAN             NOT NULL DEFAULT FALSE
);
