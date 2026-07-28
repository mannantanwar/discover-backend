CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    public_id       UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    display_name    VARCHAR(120),
    username        VARCHAR(60) UNIQUE,
    avatar_url      TEXT,
    auth_provider   VARCHAR(30) NOT NULL,
    provider_sub    VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE interaction_events (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT REFERENCES users(id),
    event_type   VARCHAR(50) NOT NULL,
    entity_type  VARCHAR(50),
    entity_id    BIGINT,
    context      JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_user_time ON interaction_events (user_id, created_at DESC);
CREATE INDEX idx_events_type      ON interaction_events (event_type);
