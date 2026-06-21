CREATE SEQUENCE users_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE users
(
    id         BIGINT PRIMARY KEY,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE short_urls_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE short_urls
(
    id           BIGINT PRIMARY KEY,
    short_key    VARCHAR(10) NOT NULL,
    original_url TEXT        NOT NULL,
    is_private   BOOLEAN     NOT NULL DEFAULT FALSE,
    expires_at   TIMESTAMP WITH TIME ZONE,
    created_by   BIGINT,
    click_count  BIGINT      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_short_urls_short_key UNIQUE (short_key),
    CONSTRAINT fk_short_urls_users FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE INDEX idx_short_urls_created_by ON short_urls (created_by);
CREATE INDEX idx_short_urls_expires_at ON short_urls (expires_at);

