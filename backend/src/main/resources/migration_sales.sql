USE DMS;

-- ============================================================
-- 1. Invoice Counter (NEW TABLE)
-- ============================================================
CREATE TABLE IF NOT EXISTS invoice_counter (
    year INT PRIMARY KEY,
    last_sequence BIGINT NOT NULL DEFAULT 0
);

-- ============================================================
-- 2. ALTER EXISTING SALES TABLE (ADD NEW COLUMNS ONLY)
-- ============================================================

ALTER TABLE sales
ADD COLUMN payment_mode VARCHAR(20) DEFAULT 'CASH',
ADD COLUMN loan_amount DECIMAL(12,2),
ADD COLUMN finance_bank VARCHAR(100),
ADD COLUMN exchange_vehicle VARCHAR(200),
ADD COLUMN exchange_value DECIMAL(12,2),
ADD COLUMN remarks TEXT,
ADD COLUMN updated_at TIMESTAMP NULL;

-- ============================================================
-- 3. ADD BUSINESS CONSTRAINT (ONE SALE PER BOOKING)
-- ============================================================

ALTER TABLE sales
ADD CONSTRAINT uk_sales_booking UNIQUE (booking_id);

-- ============================================================
-- 4. ADD DEFAULT FOR PAYMENT STATUS (IF NEEDED)
-- ============================================================

ALTER TABLE sales
MODIFY payment_status VARCHAR(20) DEFAULT 'PENDING';

-- ============================================================
-- 5. INDEXES (SAFE PERFORMANCE IMPROVEMENTS)
-- ============================================================

CREATE INDEX idx_sale_invoice ON sales(invoice_number);
CREATE INDEX idx_sale_vin ON sales(vin);
CREATE INDEX idx_sale_date ON sales(sale_date);