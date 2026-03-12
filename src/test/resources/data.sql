INSERT INTO users (id, email, password, name, phone, role, active, created_at, updated_at)
VALUES
  (1, 'customer@test.com', 'testpass', 'Test Customer', '1111111111', 'CUSTOMER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'driver@test.com', 'testpass', 'Test Driver', '2222222222', 'DRIVER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO menu_items (id, name, description, price, category, available, preparation_time_minutes, created_at, updated_at)
VALUES
  (1, 'Burger', 'Test burger', 9.99, 'Main', true, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO inventory (id, menu_item_id, quantity_available, quantity_reserved, low_stock_threshold, created_at, updated_at)
VALUES
  (1, 1, 100, 0, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
