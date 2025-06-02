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

-- REFRESH TOKEN TABLE
CREATE TABLE refresh_token
(
    id         UUID PRIMARY KEY,
    user_id    UUID                NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP           NOT NULL,
    revoked    BOOLEAN             NOT NULL DEFAULT FALSE
);

-- VERIFICATION TOKENS
CREATE TABLE verification_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP    NOT NULL
);

-- PASSWORD RESET TOKENS
CREATE TABLE password_reset_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP    NOT NULL
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

-- BOOK TABLE
CREATE TABLE books
(
    id        UUID PRIMARY KEY,
    title     VARCHAR(255) NOT NULL,
    author    VARCHAR(255) NOT NULL,
    available BOOLEAN      NOT NULL DEFAULT TRUE
);

-- RENT TABLE
CREATE TABLE rent
(
    id          UUID PRIMARY KEY,
    user_id     UUID      NOT NULL REFERENCES users (id),
    book_id     UUID      NOT NULL REFERENCES books (id),
    rented_at   TIMESTAMP NOT NULL,
    due_at      TIMESTAMP NOT NULL,
    returned_at TIMESTAMP
);
