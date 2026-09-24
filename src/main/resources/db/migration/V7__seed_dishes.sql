-- Seed data: a handful of real, well-known dishes for a few of the already-seeded
-- Connaught Place restaurants. Prices are realistic estimates, not verified menu prices —
-- worth double-checking before treating them as accurate for anything beyond testing.

INSERT INTO dishes (name, description, price, taste_tags, place_id) VALUES
('Chicken à la Kiev', 'Breaded chicken cutlet stuffed with garlic butter — a United Coffee House signature since decades.', 450.00, ARRAY['non-vegetarian','must-try','rich'], (SELECT id FROM places WHERE name = 'United Coffee House')),
('Mutton Cutlet', 'Classic pan-fried minced mutton cutlet, old-Delhi continental style.', 350.00, ARRAY['non-vegetarian','fried'], (SELECT id FROM places WHERE name = 'United Coffee House')),
('Irish Coffee', 'The restaurant''s well-known spiked coffee, served table-side.', 250.00, ARRAY['beverage','must-try'], (SELECT id FROM places WHERE name = 'United Coffee House')),

('Chicken Patty', 'Wenger''s iconic flaky patty, a Delhi bakery classic since 1926.', 90.00, ARRAY['non-vegetarian','snack','must-try'], (SELECT id FROM places WHERE name = 'Wenger''s')),
('Pineapple Pastry', 'Light sponge cake with pineapple and cream — a long-standing favorite.', 120.00, ARRAY['vegetarian','dessert'], (SELECT id FROM places WHERE name = 'Wenger''s')),
('Chicken Puff', 'Crisp puff pastry with a spiced chicken filling.', 70.00, ARRAY['non-vegetarian','snack'], (SELECT id FROM places WHERE name = 'Wenger''s')),

('Masala Dosa', 'Crisp fermented rice-and-lentil crepe with spiced potato filling.', 180.00, ARRAY['vegetarian','south-indian','must-try'], (SELECT id FROM places WHERE name = 'Saravana Bhavan')),
('Idli Sambar', 'Steamed rice cakes served with lentil sambar and coconut chutney.', 150.00, ARRAY['vegetarian','south-indian','light'], (SELECT id FROM places WHERE name = 'Saravana Bhavan')),
('Filter Coffee', 'South Indian filter coffee, served frothy in the traditional tumbler-davara.', 90.00, ARRAY['beverage','vegetarian'], (SELECT id FROM places WHERE name = 'Saravana Bhavan')),

('Butter Chicken', 'Kake Da Hotel''s most famous dish — tandoori chicken in a rich tomato-butter gravy.', 450.00, ARRAY['non-vegetarian','must-try','rich','spicy'], (SELECT id FROM places WHERE name = 'Kake Da Hotel')),
('Dal Makhani', 'Slow-cooked black lentils finished with butter and cream.', 280.00, ARRAY['vegetarian','rich'], (SELECT id FROM places WHERE name = 'Kake Da Hotel')),
('Tandoori Roti', 'Whole-wheat flatbread cooked in a clay tandoor.', 40.00, ARRAY['vegetarian','bread'], (SELECT id FROM places WHERE name = 'Kake Da Hotel')),

('Chicken Kathi Roll', 'Spiced chicken wrapped in a flaky paratha — the dish this place is named for.', 180.00, ARRAY['non-vegetarian','must-try','spicy'], (SELECT id FROM places WHERE name = 'Nizam''s Kathi Kabab')),
('Mutton Kathi Roll', 'Same iconic roll, with spiced minced mutton instead of chicken.', 220.00, ARRAY['non-vegetarian','spicy'], (SELECT id FROM places WHERE name = 'Nizam''s Kathi Kabab')),
('Egg Roll', 'A lighter, egg-based version of the classic kathi roll.', 100.00, ARRAY['non-vegetarian','light'], (SELECT id FROM places WHERE name = 'Nizam''s Kathi Kabab'));
