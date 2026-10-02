-- Enriches the V7 dishes with descriptive tags (flavor, texture, temperature, meal-time) using the
-- same vocabulary the context rules emit, so home-feed context sections have something to match.
-- Full replacement per dish (existing tags kept, new ones added) rather than appending, so the
-- final tag list for each dish is readable in one place and can't pick up duplicates.

UPDATE dishes SET taste_tags = ARRAY['non-vegetarian','must-try','rich','fried','crispy','buttery','savory','main-course','lunch','dinner','comfort-food'] WHERE name = 'Chicken à la Kiev';
UPDATE dishes SET taste_tags = ARRAY['non-vegetarian','fried','crispy','savory','snack','comfort-food'] WHERE name = 'Mutton Cutlet';
UPDATE dishes SET taste_tags = ARRAY['beverage','must-try','coffee','hot','warm','creamy'] WHERE name = 'Irish Coffee';

UPDATE dishes SET taste_tags = ARRAY['non-vegetarian','snack','must-try','baked','crispy','savory','comfort-food'] WHERE name = 'Chicken Patty';
UPDATE dishes SET taste_tags = ARRAY['vegetarian','dessert','sweet','creamy','baked','fruity'] WHERE name = 'Pineapple Pastry';
UPDATE dishes SET taste_tags = ARRAY['non-vegetarian','snack','baked','crispy','savory','spicy'] WHERE name = 'Chicken Puff';

UPDATE dishes SET taste_tags = ARRAY['vegetarian','south-indian','must-try','crispy','savory','spicy','breakfast','filling'] WHERE name = 'Masala Dosa';
UPDATE dishes SET taste_tags = ARRAY['vegetarian','south-indian','light','healthy','breakfast','soupy','warm'] WHERE name = 'Idli Sambar';
UPDATE dishes SET taste_tags = ARRAY['beverage','vegetarian','coffee','hot','warm','milky','south-indian','breakfast'] WHERE name = 'Filter Coffee';

UPDATE dishes SET taste_tags = ARRAY['non-vegetarian','must-try','rich','spicy','creamy','buttery','curry','north-indian','main-course','dinner','comfort-food'] WHERE name = 'Butter Chicken';
UPDATE dishes SET taste_tags = ARRAY['vegetarian','rich','creamy','buttery','slow-cooked','curry','north-indian','main-course','comfort-food','warm'] WHERE name = 'Dal Makhani';
UPDATE dishes SET taste_tags = ARRAY['vegetarian','bread','tandoori','north-indian'] WHERE name = 'Tandoori Roti';

UPDATE dishes SET taste_tags = ARRAY['non-vegetarian','must-try','spicy','street-food','grilled','savory','filling','snack'] WHERE name = 'Chicken Kathi Roll';
UPDATE dishes SET taste_tags = ARRAY['non-vegetarian','spicy','street-food','grilled','savory','filling'] WHERE name = 'Mutton Kathi Roll';
UPDATE dishes SET taste_tags = ARRAY['non-vegetarian','light','eggy','street-food','snack','savory'] WHERE name = 'Egg Roll';

-- New dishes at already-seeded places that had none, chosen so each context rule (rain, hot day,
-- Holi, Janmashtami, Diwali, every time-of-day slot) has at least one real match nearby.
-- Same caveat as V7: well-known items for these places, but prices are estimates and exact
-- menu availability (especially seasonal items like Thandai) hasn't been verified.

INSERT INTO dishes (name, description, price, taste_tags, place_id) VALUES
('Masala Chai', 'Milky spiced tea brewed with ginger and cardamom.', 40.00, ARRAY['beverage','tea','hot','warm','milky','gingery','cardamom','vegetarian','breakfast'], (SELECT id FROM places WHERE name = 'Haldiram''s')),
('Samosa', 'Crisp fried pastry stuffed with spiced potato and peas.', 30.00, ARRAY['vegetarian','snack','fried','crispy','spicy','savory','street-food','comfort-food'], (SELECT id FROM places WHERE name = 'Haldiram''s')),
('Raj Kachori', 'Large crisp kachori filled with chaat, yogurt and sweet-tangy chutneys.', 150.00, ARRAY['vegetarian','chaat','tangy','sweet','yogurt-based','street-food','snack','crispy'], (SELECT id FROM places WHERE name = 'Haldiram''s')),
('Gulab Jamun', 'Soft milk-solid dumplings soaked in warm cardamom sugar syrup.', 80.00, ARRAY['vegetarian','dessert','sweet','syrupy','mithai','festive','warm','milky','cardamom'], (SELECT id FROM places WHERE name = 'Haldiram''s')),
('Kaju Katli', 'Thin cashew fudge diamonds, a Diwali gifting staple.', 300.00, ARRAY['vegetarian','dessert','sweet','mithai','nutty','dry-fruit','festive'], (SELECT id FROM places WHERE name = 'Haldiram''s')),
('Rasmalai', 'Soft paneer discs in chilled saffron-cardamom milk.', 150.00, ARRAY['vegetarian','dessert','sweet','milky','creamy','saffron','cardamom','nutty','mithai','festive','chilled'], (SELECT id FROM places WHERE name = 'Haldiram''s')),
('Thandai', 'Chilled milk with nuts, saffron and spices — a Holi classic.', 120.00, ARRAY['beverage','vegetarian','chilled','cooling','refreshing','milky','nutty','saffron','cardamom','festive','sweet'], (SELECT id FROM places WHERE name = 'Haldiram''s')),

('Signature Hot Chocolate', 'Thick, rich hot chocolate topped with whipped cream.', 300.00, ARRAY['beverage','vegetarian','hot','warm','chocolatey','milky','creamy','thick','sweet'], (SELECT id FROM places WHERE name = 'Starbucks Reserve')),
('Java Chip Frappuccino', 'Blended iced coffee with chocolate chips and whipped cream.', 350.00, ARRAY['beverage','vegetarian','cold','chilled','icy','chocolatey','coffee','sweet','creamy','refreshing'], (SELECT id FROM places WHERE name = 'Starbucks Reserve')),

('Cream of Tomato Soup', 'Classic creamy tomato soup served with bread croutons.', 220.00, ARRAY['vegetarian','soupy','hot','warm','creamy','tangy','light','comfort-food'], (SELECT id FROM places WHERE name = 'The Embassy Restaurant')),

('Sweet Lassi', 'Thick chilled yogurt drink, Punjabi style.', 120.00, ARRAY['beverage','vegetarian','chilled','cooling','refreshing','yogurt-based','sweet','thick'], (SELECT id FROM places WHERE name = 'Pind Balluchi'));
