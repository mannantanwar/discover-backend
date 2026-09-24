CREATE TABLE dishes (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    place_id BIGINT NOT NULL REFERENCES places(id),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    price NUMERIC(10,2) NOT NULL,
    taste_tags TEXT[],
    photo_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_dishes_place_id ON dishes(place_id);
