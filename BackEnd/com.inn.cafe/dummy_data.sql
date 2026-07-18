-- Approved login users (BCrypt-hashed passwords: admin@123 / user@123)
INSERT INTO "user" (id, name, email, contact_number, password, role, status) VALUES
 (10, 'Cafe Admin', 'admin@cafe.com', '9000000001', '$2b$10$FaieajF7Fgc1iL8HYGmqJOKixt7SQbYFjpcH1Fchnx4jFbGSeYZ86', 'admin', 'true'),
 (11, 'John User', 'user@cafe.com', '9000000002', '$2b$10$tp.LPnyHt1hDcriFZe/Hpe7RuJD3WAEfsB5EMzutkup6d8zTYG4v6', 'user', 'true');

-- Approve any previously self-registered accounts as well
UPDATE "user" SET status = 'true' WHERE status IS NULL;

-- Categories
INSERT INTO category (id, name) VALUES
 (20, 'Pizza'),
 (21, 'Biryani'),
 (22, 'Beverages'),
 (23, 'Desserts');

-- Products (price is INT, status 'true' = active)
INSERT INTO product (id, name, description, price, status, category_fk,
                      is_veg, spicy_level, best_seller, new_arrival, rating, rating_count, image_url, prep_time_minutes) VALUES
 (30, 'Margherita Pizza', 'Classic cheese & tomato pizza', 250, 'true', 20, true, 'NONE', true, false, 4.5, 120, '/assets/img/1.jpg', 15),
 (31, 'Farmhouse Pizza', 'Loaded with fresh veggies', 350, 'true', 20, true, 'MILD', true, false, 4.6, 95, '/assets/img/1.jpg', 18),
 (32, 'Chicken Biryani', 'Hyderabadi dum biryani', 300, 'true', 21, false, 'MEDIUM', true, false, 4.7, 210, '/assets/img/2.jpg', 25),
 (33, 'Veg Biryani', 'Aromatic vegetable biryani', 220, 'true', 21, true, 'MILD', false, true, 4.3, 60, '/assets/img/2.jpg', 20),
 (34, 'Cold Coffee', 'Chilled creamy coffee', 120, 'true', 22, true, 'NONE', false, true, 4.2, 40, '/assets/img/3.jpg', 5),
 (35, 'Masala Chai', 'Spiced Indian tea', 40, 'true', 22, true, 'NONE', false, false, 4.0, 30, '/assets/img/3.jpg', 5),
 (36, 'Chocolate Lava Cake', 'Warm molten chocolate cake', 150, 'true', 23, true, 'NONE', true, false, 4.8, 150, '/assets/img/4.jpg', 12),
 (37, 'Gulab Jamun', 'Two pieces in sugar syrup', 80, 'true', 23, true, 'NONE', false, true, 4.1, 25, '/assets/img/4.jpg', 5);

-- Keep Hibernate's per-entity sequences (Hibernate 6 / Boot 3 style) ahead of our manual ids
SELECT setval('user_seq', 100, false);
SELECT setval('category_seq', 100, false);
SELECT setval('product_seq', 100, false);
SELECT setval('bill_seq', 100, false);
