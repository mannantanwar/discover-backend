CREATE TABLE dish_reviews (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    dish_id BIGINT NOT NULL REFERENCES dishes(id),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, dish_id)
);

CREATE INDEX idx_dish_reviews_dish_id ON dish_reviews(dish_id);
