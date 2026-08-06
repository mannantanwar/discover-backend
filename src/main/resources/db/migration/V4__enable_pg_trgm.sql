CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_places_name_trgm ON places USING GIN (name gin_trgm_ops);
