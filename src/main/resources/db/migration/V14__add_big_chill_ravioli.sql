-- Price is an estimate, not a verified menu price (same caveat as V13).

INSERT INTO dishes (name, description, price, taste_tags, place_id) VALUES
('Ravioli ai Funghi', 'Mushroom-filled ravioli in a creamy, cheesy sauce.', 545.00, ARRAY['vegetarian','italian','main-course','mushroom','creamy','cheesy','rich','comfort-food','dinner'], (SELECT id FROM places WHERE name = 'Big Chill'));
