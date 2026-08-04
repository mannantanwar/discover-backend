CREATE TABLE places(
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(180) NOT NULL,
    description TEXT,
    address VARCHAR(500),
    location GEOGRAPHY(POINT, 4326) NOT NULL,
    budget_level SMALLINT,
    opening_hours JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_places_location ON places USING GIST(location);
