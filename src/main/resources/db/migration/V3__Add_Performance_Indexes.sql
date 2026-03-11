-- V3__Add_Performance_Indexes.sql
-- Additional indexes for performance optimization

-- Order query performance
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);
CREATE INDEX idx_orders_status_created ON orders(status, created_at DESC);

-- Payment query performance
CREATE INDEX idx_payments_order_status ON payments(order_id, status);
CREATE INDEX idx_payments_created_at_status ON payments(created_at DESC, status);

-- Order items query performance
CREATE INDEX idx_order_items_order_menu ON order_items(order_id, menu_item_id);

-- Inventory query performance
CREATE INDEX idx_inventory_available ON inventory(quantity_available - quantity_reserved);

-- Preparation query performance
CREATE INDEX idx_preparations_order_status ON preparations(order_id, status);

-- Delivery query performance
CREATE INDEX idx_deliveries_driver_status ON deliveries(driver_id, status);
CREATE INDEX idx_deliveries_status_created ON deliveries(status, created_at DESC);

-- User query performance
CREATE INDEX idx_users_role_active ON users(role, active);

