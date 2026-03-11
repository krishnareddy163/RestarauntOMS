-- V2__Insert_Initial_Data.sql
-- Initial data population for RestaurantOS

-- Insert Admin User
INSERT INTO users (email, password, name, phone, role, active)
VALUES ('admin@restaurantos.com', '$2a$10$PH8xfHC91vwZIx/VqNZt8OV5D7nDKEfVRG9FvNmMXFvVNvTWTJKu2', 'Admin User', '+1-555-0001', 'ADMIN', true)
ON CONFLICT (email) DO NOTHING;

-- Insert Kitchen Staff
INSERT INTO users (email, password, name, phone, role, active)
VALUES
('kitchen1@restaurantos.com', '$2a$10$PH8xfHC91vwZIx/VqNZt8OV5D7nDKEfVRG9FvNmMXFvVNvTWTJKu2', 'Kitchen Staff 1', '+1-555-0002', 'KITCHEN_STAFF', true),
('kitchen2@restaurantos.com', '$2a$10$PH8xfHC91vwZIx/VqNZt8OV5D7nDKEfVRG9FvNmMXFvVNvTWTJKu2', 'Kitchen Staff 2', '+1-555-0003', 'KITCHEN_STAFF', true)
ON CONFLICT (email) DO NOTHING;

-- Insert Drivers
INSERT INTO users (email, password, name, phone, role, active)
VALUES
('driver1@restaurantos.com', '$2a$10$PH8xfHC91vwZIx/VqNZt8OV5D7nDKEfVRG9FvNmMXFvVNvTWTJKu2', 'Driver 1', '+1-555-0004', 'DRIVER', true),
('driver2@restaurantos.com', '$2a$10$PH8xfHC91vwZIx/VqNZt8OV5D7nDKEfVRG9FvNmMXFvVNvTWTJKu2', 'Driver 2', '+1-555-0005', 'DRIVER', true),
('driver3@restaurantos.com', '$2a$10$PH8xfHC91vwZIx/VqNZt8OV5D7nDKEfVRG9FvNmMXFvVNvTWTJKu2', 'Driver 3', '+1-555-0006', 'DRIVER', true)
ON CONFLICT (email) DO NOTHING;

-- Insert Manager
INSERT INTO users (email, password, name, phone, role, active)
VALUES ('manager@restaurantos.com', '$2a$10$PH8xfHC91vwZIx/VqNZt8OV5D7nDKEfVRG9FvNmMXFvVNvTWTJKu2', 'Manager', '+1-555-0007', 'MANAGER', true)
ON CONFLICT (email) DO NOTHING;

-- Insert Sample Customers
INSERT INTO users (email, password, name, phone, role, active)
VALUES
('customer1@example.com', '$2a$10$PH8xfHC91vwZIx/VqNZt8OV5D7nDKEfVRG9FvNmMXFvVNvTWTJKu2', 'John Doe', '+1-555-1001', 'CUSTOMER', true),
('customer2@example.com', '$2a$10$PH8xfHC91vwZIx/VqNZt8OV5D7nDKEfVRG9FvNmMXFvVNvTWTJKu2', 'Jane Smith', '+1-555-1002', 'CUSTOMER', true),
('customer3@example.com', '$2a$10$PH8xfHC91vwZIx/VqNZt8OV5D7nDKEfVRG9FvNmMXFvVNvTWTJKu2', 'Bob Johnson', '+1-555-1003', 'CUSTOMER', true)
ON CONFLICT (email) DO NOTHING;

-- Insert Menu Items
INSERT INTO menu_items (name, description, price, category, preparation_time_minutes, available)
VALUES
('Margherita Pizza', 'Classic pizza with tomato, mozzarella, and basil', 12.99, 'Pizza', 15, true),
('Pepperoni Pizza', 'Pizza topped with pepperoni and cheese', 14.99, 'Pizza', 15, true),
('Vegetarian Pizza', 'Pizza with assorted vegetables', 13.99, 'Pizza', 15, true),
('Grilled Chicken Burger', 'Juicy grilled chicken breast on bun', 10.99, 'Burgers', 10, true),
('Beef Burger', 'Classic beef patty burger', 11.99, 'Burgers', 10, true),
('Caesar Salad', 'Fresh romaine lettuce with Caesar dressing', 8.99, 'Salads', 5, true),
('Greek Salad', 'Tomato, cucumber, feta cheese, olives', 9.99, 'Salads', 5, true),
('Spaghetti Carbonara', 'Pasta with bacon and creamy sauce', 13.99, 'Pasta', 12, true),
('Penne Arrabbiata', 'Spicy tomato pasta dish', 12.99, 'Pasta', 12, true),
('Tiramisu', 'Classic Italian dessert', 5.99, 'Desserts', 2, true),
('Chocolate Cake', 'Rich chocolate layer cake', 4.99, 'Desserts', 2, true),
('Coca Cola', 'Soft drink', 2.49, 'Beverages', 1, true),
('Iced Tea', 'Refreshing iced tea', 2.99, 'Beverages', 1, true)
ON CONFLICT DO NOTHING;

-- Insert Inventory for Menu Items
INSERT INTO inventory (menu_item_id, quantity_available, quantity_reserved, low_stock_threshold)
SELECT id, 50, 0, 10 FROM menu_items
ON CONFLICT (menu_item_id) DO NOTHING;

