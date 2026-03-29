-- ============================================
-- Dealer Management System (DMS) Database
-- ============================================
CREATE DATABASE DMS;
USE DMS;
-- ============================================
-- 1. Roles
-- ============================================
CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- ============================================
-- 2. Permissions
-- ============================================
CREATE TABLE permissions (
    permission_id SERIAL PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- ============================================
-- 3. Role Permissions
-- ============================================
CREATE TABLE role_permissions (
    role_permission_id SERIAL PRIMARY KEY,
    role_id INT REFERENCES roles(role_id),
    permission_id INT REFERENCES permissions(permission_id)
);

-- ============================================
-- 4. States
-- ============================================
CREATE TABLE states (
    state_id SERIAL PRIMARY KEY,
    state_name VARCHAR(100) NOT NULL
);

-- ============================================
-- 5. Cities
-- ============================================
CREATE TABLE cities (
    city_id SERIAL PRIMARY KEY,
    city_name VARCHAR(100) NOT NULL,
    state_id INT REFERENCES states(state_id)
);

-- ============================================
-- 6. Dealers
-- ============================================
CREATE TABLE dealers (
    dealer_id SERIAL PRIMARY KEY,
    dealer_name VARCHAR(150) NOT NULL,
    dealer_code VARCHAR(50) UNIQUE NOT NULL,
    address TEXT,
    city_id INT REFERENCES cities(city_id),
    state_id INT REFERENCES states(state_id),
    phone VARCHAR(20),
    email VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 7. Users
-- ============================================
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role_id INT REFERENCES roles(role_id),
    dealer_id INT REFERENCES dealers(dealer_id),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 8. Login Logs
-- ============================================
CREATE TABLE login_logs (
    log_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id),
    login_time TIMESTAMP,
    logout_time TIMESTAMP,
    ip_address VARCHAR(50)
);

-- ============================================
-- 9. Vehicle Models
-- ============================================
CREATE TABLE models (
    model_id SERIAL PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    variant VARCHAR(100),
    fuel_type VARCHAR(20),
    transmission VARCHAR(20),
    ex_showroom_price DECIMAL(12,2),
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- ============================================
-- 10. Vehicle Stock (VIN-level)
-- ============================================
CREATE TABLE vehicle_stock (
    vin VARCHAR(50) PRIMARY KEY,
    model_id INT REFERENCES models(model_id),
    dealer_id INT REFERENCES dealers(dealer_id),
    color VARCHAR(50),
    manufacture_date DATE,
    stock_status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 11. Customers
-- ============================================
CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    customer_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 12. Enquiries
-- ============================================
CREATE TABLE enquiries (
    enquiry_id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES customers(customer_id),
    model_id INT REFERENCES models(model_id),
    dealer_id INT REFERENCES dealers(dealer_id),
    source VARCHAR(50),
    enquiry_date DATE,
    status VARCHAR(20) DEFAULT 'NEW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 13. Bookings
-- ============================================
CREATE TABLE bookings (
    booking_id SERIAL PRIMARY KEY,
    enquiry_id INT REFERENCES enquiries(enquiry_id),
    vin VARCHAR(50) REFERENCES vehicle_stock(vin),
    dealer_id INT REFERENCES dealers(dealer_id),
    booking_date DATE,
    booking_amount DECIMAL(12,2),
    booking_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 14. Sales
-- ============================================
CREATE TABLE sales (
    sale_id SERIAL PRIMARY KEY,
    booking_id INT REFERENCES bookings(booking_id),
    vin VARCHAR(50) REFERENCES vehicle_stock(vin),
    invoice_number VARCHAR(100) UNIQUE,
    sale_date DATE,
    sale_price DECIMAL(12,2),
    payment_status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 15. Menus
-- ============================================
CREATE TABLE menus (
    menu_id SERIAL PRIMARY KEY,
    menu_name VARCHAR(100),
    route VARCHAR(150),
    icon VARCHAR(50),
    display_order INT,
    active_flag BOOLEAN DEFAULT TRUE
);

-- ============================================
-- 16. Submenus
-- ============================================
CREATE TABLE submenus (
    submenu_id SERIAL PRIMARY KEY,
    menu_id INT REFERENCES menus(menu_id),
    submenu_name VARCHAR(100),
    route VARCHAR(150),
    display_order INT,
    active_flag BOOLEAN DEFAULT TRUE
);

-- ============================================
-- 17. Role Menu Mapping
-- ============================================
CREATE TABLE role_menu_map (
    id SERIAL PRIMARY KEY,
    role_id INT REFERENCES roles(role_id),
    menu_id INT REFERENCES menus(menu_id)
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX idx_vehicle_dealer ON vehicle_stock(dealer_id);
CREATE INDEX idx_enquiry_dealer ON enquiries(dealer_id);
CREATE INDEX idx_booking_enquiry ON bookings(enquiry_id);

CREATE INDEX idx_vin ON vehicle_stock(vin);
CREATE INDEX idx_dealer ON dealers(dealer_id);
CREATE INDEX idx_enquiry ON enquiries(enquiry_id);
CREATE INDEX idx_booking ON bookings(booking_id);

-- ============================================
-- END OF DMS DATABASE SCHEMA
-- ============================================