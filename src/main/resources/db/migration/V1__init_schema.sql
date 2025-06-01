-- USERS TABLE
CREATE TABLE users
(
    id             UUID PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    role           VARCHAR(32)  NOT NULL,
    email_verified BOOLEAN   DEFAULT FALSE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- VERIFICATION TOKENS
CREATE TABLE verification_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL
);

-- PASSWORD RESET TOKENS
CREATE TABLE password_reset_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL
);

-- PASSWORD CHANGE LOG
CREATE TABLE password_change_log
(
    id                  UUID PRIMARY KEY,
    user_id             UUID REFERENCES users (id) ON DELETE CASCADE,
    old_hashed_password VARCHAR(255) NOT NULL,
    changed_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ACTIVITY AUDIT TRAIL (LOGIN, LOGOUT, RESET, VERIFY, etc)
CREATE TABLE user_activity_audit
(
    id            UUID PRIMARY KEY,
    user_id       UUID REFERENCES users (id),
    email         VARCHAR(255),
    activity_type VARCHAR(64) NOT NULL, -- LOGIN, LOGOUT, VERIFY_EMAIL, RESET_PASSWORD
    success       BOOLEAN     NOT NULL,
    ip_address    VARCHAR(64),
    user_agent    TEXT,
    activity_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);