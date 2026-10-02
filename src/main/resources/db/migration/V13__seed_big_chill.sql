-- Big Chill Cafe, Connaught Place outlet, plus a selection of its well-known menu items.
-- Coordinates are an approximate Connaught Place location (same caveat as V3), not the exact
-- outlet geocode. Dishes are items Big Chill is known for; prices are estimates, not verified
-- menu prices. Desserts deliberately aren't tagged 'vegetarian' — several may contain egg.

INSERT INTO places (name, category, description, address, location, budget_level, opening_hours) VALUES
('Big Chill', 'cafe', 'Popular Delhi cafe known for its Italian mains, shakes and indulgent desserts.', 'Connaught Place, New Delhi', ST_SetSRID(ST_MakePoint(77.2188, 28.6316), 4326)::geography, 3, '{"open": "12:00", "close": "23:30"}'::jsonb);

INSERT INTO dishes (name, description, price, taste_tags, place_id) VALUES
('Penne Rosa', 'Penne in a creamy tomato-pink sauce.', 495.00, ARRAY['vegetarian','italian','main-course','creamy','tangy','filling','comfort-food','lunch','dinner'], (SELECT id FROM places WHERE name = 'Big Chill')),
('Spaghetti Aglio e Olio', 'Spaghetti tossed in garlic, olive oil and chilli flakes.', 445.00, ARRAY['vegetarian','italian','main-course','light','savory','spicy','lunch'], (SELECT id FROM places WHERE name = 'Big Chill')),
('Chicken Lasagne', 'Layered pasta baked with chicken ragu, bechamel and cheese.', 595.00, ARRAY['non-vegetarian','italian','main-course','rich','cheesy','baked','filling','comfort-food','dinner'], (SELECT id FROM places WHERE name = 'Big Chill')),
('Margherita Pizza', 'Thin-crust pizza with tomato, mozzarella and basil.', 495.00, ARRAY['vegetarian','italian','cheesy','baked','savory','main-course','lunch','dinner'], (SELECT id FROM places WHERE name = 'Big Chill')),
('Caesar Salad', 'Crisp lettuce with parmesan, croutons and Caesar dressing.', 395.00, ARRAY['continental','salad','light','healthy','fresh','savory','crispy','lunch'], (SELECT id FROM places WHERE name = 'Big Chill')),
('Mississippi Mud Pie', 'Big Chill''s signature dense chocolate pie — a must-order.', 345.00, ARRAY['dessert','sweet','chocolatey','rich','thick','must-try'], (SELECT id FROM places WHERE name = 'Big Chill')),
('Nutella Cheesecake', 'Creamy baked cheesecake swirled with Nutella.', 395.00, ARRAY['dessert','sweet','chocolatey','creamy','nutty','rich','baked'], (SELECT id FROM places WHERE name = 'Big Chill')),
('Oreo Shake', 'Thick milkshake blended with Oreo cookies and ice cream.', 295.00, ARRAY['beverage','vegetarian','chilled','cold','sweet','chocolatey','creamy','thick','milky'], (SELECT id FROM places WHERE name = 'Big Chill')),
('Lemon Iced Tea', 'Freshly brewed iced tea with lemon.', 225.00, ARRAY['beverage','vegetarian','tea','chilled','cold','refreshing','citrusy','tangy','light'], (SELECT id FROM places WHERE name = 'Big Chill'));
