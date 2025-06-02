CREATE TABLE rent
(
    id          UUID PRIMARY KEY,
    user_id     UUID      NOT NULL REFERENCES users (id),
    book_id     UUID      NOT NULL REFERENCES books (id),
    rented_at   TIMESTAMP NOT NULL,
    due_at      TIMESTAMP NOT NULL,
    returned_at TIMESTAMP
);
