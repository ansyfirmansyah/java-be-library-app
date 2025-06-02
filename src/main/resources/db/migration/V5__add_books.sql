CREATE TABLE books
(
    id        UUID PRIMARY KEY,
    title     VARCHAR(255) NOT NULL,
    author    VARCHAR(255) NOT NULL,
    available BOOLEAN      NOT NULL DEFAULT TRUE
);