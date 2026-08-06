-- Seed data: Connaught Place, New Delhi — one neighborhood, ~12 real places to start.
-- Coordinates are approximate (roughly located by block, not exact building geocodes) —
-- worth spot-checking a few against Google Maps before relying on them for real "near me" testing.

INSERT INTO places (name, category, description, address, location, budget_level, opening_hours) VALUES
('United Coffee House', 'restaurant', 'Iconic multi-cuisine restaurant, open since 1942, known for its old-world interiors.', 'E-Block, Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2183, 28.6320), 4326)::geography, 3, '{"open": "09:00", "close": "23:00"}'::jsonb),

('Wenger''s', 'bakery', 'Legendary Delhi bakery since 1926, famous for patties and pastries.', 'A-Block, Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2178, 28.6328), 4326)::geography, 1, '{"open": "10:00", "close": "20:30"}'::jsonb),

('Saravana Bhavan', 'restaurant', 'Popular South Indian vegetarian restaurant chain.', 'Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2170, 28.6305), 4326)::geography, 2, '{"open": "08:00", "close": "23:00"}'::jsonb),

('Zaffran', 'restaurant', 'North Indian and Mughlai cuisine restaurant.', 'N-Block, Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2200, 28.6300), 4326)::geography, 3, '{"open": "12:00", "close": "23:30"}'::jsonb),

('Kake Da Hotel', 'restaurant', 'Long-running Punjabi dhaba-style restaurant near Connaught Place.', 'Radial Road No. 4, Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2165, 28.6340), 4326)::geography, 2, '{"open": "08:00", "close": "23:00"}'::jsonb),

('Nizam''s Kathi Kabab', 'restaurant', 'Famous for kathi rolls, a Delhi institution.', 'H-Block, Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2210, 28.6318), 4326)::geography, 2, '{"open": "11:00", "close": "23:00"}'::jsonb),

('Haldiram''s', 'restaurant', 'Popular Indian sweets, snacks, and fast-food chain.', 'Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2190, 28.6295), 4326)::geography, 1, '{"open": "08:00", "close": "23:00"}'::jsonb),

('Starbucks Reserve', 'cafe', 'Large Starbucks outlet with a reserve coffee bar.', 'Inner Circle, Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2195, 28.6312), 4326)::geography, 3, '{"open": "08:00", "close": "23:00"}'::jsonb),

('The Embassy Restaurant', 'restaurant', 'Old-Delhi institution serving continental and Indian dishes since 1948.', 'D-Block, Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2185, 28.6325), 4326)::geography, 3, '{"open": "10:00", "close": "23:00"}'::jsonb),

('Q''BA', 'restaurant', 'Rooftop multi-cuisine restaurant and bar.', 'Outer Circle, Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2220, 28.6330), 4326)::geography, 4, '{"open": "12:00", "close": "01:00"}'::jsonb),

('Cafe Coffee Day', 'cafe', 'Popular Indian coffee chain outlet.', 'Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2175, 28.6300), 4326)::geography, 1, '{"open": "09:00", "close": "23:00"}'::jsonb),

('Pind Balluchi', 'restaurant', 'Punjabi-themed multi-cuisine restaurant chain.', 'Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2205, 28.6290), 4326)::geography, 3, '{"open": "11:00", "close": "23:30"}'::jsonb);
