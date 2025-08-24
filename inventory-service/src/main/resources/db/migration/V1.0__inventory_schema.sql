-- ===================================================
-- INVENTORY SERVICE SCHEMA
-- ===================================================

-- Inventory lot status
CREATE TYPE IF NOT EXISTS lot_status AS ENUM('ACTIVE', 'QUARANTINED', 'EXPIRED', 'DISPOSED');

-- Inventory item status
CREATE TYPE IF NOT EXISTS inventory_item_status AS ENUM('AVAILABLE', 'ALLOCATED', 'QUARANTINED', 'DAMAGED', 'SHRINKAGE', 'IN_TRANSIT', 'RESERVED');

-- Condition codes
CREATE TYPE IF NOT EXISTS condition_code AS ENUM('NEW', 'USED', 'REFURBISHED', 'DAMAGED', 'DEFECTIVE');

-- Transaction types
CREATE TYPE IF NOT EXISTS transaction_type AS ENUM('RECEIPT', 'ISSUE', 'ADJUSTMENT', 'TRANSFER', 'RETURN', 'PHYSICAL_COUNT', 'CYCLE_COUNT', 'WRITE_OFF', 'RESERVATION', 'ALLOCATION', 'DEALLOCATION');

-- Transaction status
CREATE TYPE IF NOT EXISTS transaction_status AS ENUM('PENDING', 'COMPLETED', 'CANCELLED', 'ERROR');

-- Count types
CREATE TYPE IF NOT EXISTS count_type AS ENUM('CYCLE_COUNT', 'PHYSICAL_INVENTORY', 'SPOT_CHECK');

-- Count status
CREATE TYPE IF NOT EXISTS count_status AS ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

-- Reservation types
CREATE TYPE IF NOT EXISTS reservation_type AS ENUM('SALES_ORDER', 'TRANSFER_ORDER', 'QUALITY_CHECK', 'WORK_ORDER', 'OTHER');

-- Reservation status
CREATE TYPE IF NOT EXISTS reservation_status AS ENUM('PENDING', 'ALLOCATED', 'PARTIALLY_ALLOCATED', 'CANCELLED', 'FULFILLED');

-- Inventory policies
CREATE TABLE IF NOT EXISTS inventory_policies (
    policy_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL,
    product_id INT,
    variant_id INT,
    min_stock_level NUMERIC(12,4) DEFAULT 0,
    max_stock_level NUMERIC(12,4) DEFAULT 0,
    reorder_point NUMERIC(12,4) DEFAULT 0,
    reorder_quantity NUMERIC(12,4) DEFAULT 0,
    valuation_method VARCHAR(20) DEFAULT 'FIFO',
    abc_class CHAR(1) DEFAULT 'C',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE CASCADE
);

-- Create index for inventory policies
CREATE INDEX IF NOT EXISTS idx_inventory_policies_tenant ON inventory_policies(tenant_id);
CREATE INDEX IF NOT EXISTS idx_inventory_policies_product ON inventory_policies(product_id);

-- Inventory items
CREATE TABLE IF NOT EXISTS inventory_items (
    item_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL,
    product_id INT NOT NULL,
    variant_id INT,
    lot_number VARCHAR(100),
    serial_number VARCHAR(100),
    status inventory_item_status NOT NULL DEFAULT 'AVAILABLE',
    condition condition_code NOT NULL DEFAULT 'NEW',
    quantity_on_hand NUMERIC(12,4) NOT NULL DEFAULT 0,
    quantity_allocated NUMERIC(12,4) NOT NULL DEFAULT 0,
    quantity_available NUMERIC(12,4) GENERATED ALWAYS AS (quantity_on_hand - quantity_allocated) STORED,
    unit_of_measure VARCHAR(20) NOT NULL,
    location_id INT,
    facility_id INT NOT NULL,
    expiry_date DATE,
    manufacture_date DATE,
    received_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_counted_date TIMESTAMPTZ,
    unit_cost NUMERIC(12,4),
    total_cost NUMERIC(12,4) GENERATED ALWAYS AS (quantity_on_hand * COALESCE(unit_cost, 0)) STORED,
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    FOREIGN KEY (location_id) REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    FOREIGN KEY (facility_id) REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE
);

-- Create indexes for inventory items
CREATE INDEX IF NOT EXISTS idx_inventory_items_tenant ON inventory_items(tenant_id);
CREATE INDEX IF NOT EXISTS idx_inventory_items_product ON inventory_items(product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_items_status ON inventory_items(status);
CREATE INDEX IF NOT EXISTS idx_inventory_items_location ON inventory_items(location_id);
CREATE INDEX IF NOT EXISTS idx_inventory_items_facility ON inventory_items(facility_id);

-- Inventory transactions
CREATE TABLE IF NOT EXISTS inventory_transactions (
    transaction_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL,
    transaction_type transaction_type NOT NULL,
    transaction_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status transaction_status NOT NULL DEFAULT 'COMPLETED',
    reference_number VARCHAR(100),
    reference_type VARCHAR(50),
    reference_id INT,
    source_type VARCHAR(50),
    source_id INT,
    destination_type VARCHAR(50),
    destination_id INT,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE
);

-- Create indexes for inventory transactions
CREATE INDEX IF NOT EXISTS idx_inv_trans_tenant ON inventory_transactions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_inv_trans_type ON inventory_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_inv_trans_date ON inventory_transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_inv_trans_reference ON inventory_transactions(reference_type, reference_id);

-- Transaction details
CREATE TABLE IF NOT EXISTS inventory_transaction_details (
    transaction_detail_id SERIAL PRIMARY KEY,
    transaction_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity NUMERIC(12,4) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    unit_cost NUMERIC(12,4),
    total_cost NUMERIC(12,4) GENERATED ALWAYS AS (quantity * COALESCE(unit_cost, 0)) STORED,
    lot_number VARCHAR(100),
    serial_number VARCHAR(100),
    from_location_id INT,
    to_location_id INT,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (transaction_id) REFERENCES inventory_transactions(transaction_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (from_location_id) REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    FOREIGN KEY (to_location_id) REFERENCES storage_locations(location_id) ON DELETE SET NULL
);

-- Create indexes for transaction details
CREATE INDEX IF NOT EXISTS idx_transaction_details_transaction ON inventory_transaction_details(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_details_item ON inventory_transaction_details(item_id);

-- Inventory counts
CREATE TABLE IF NOT EXISTS inventory_counts (
    count_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL,
    count_number VARCHAR(50) NOT NULL,
    count_type count_type NOT NULL,
    status count_status NOT NULL DEFAULT 'PENDING',
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    facility_id INT,
    zone_id INT,
    location_id INT,
    product_id INT,
    category_id INT,
    notes TEXT,
    is_approved BOOLEAN DEFAULT FALSE,
    approved_by INT,
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    FOREIGN KEY (facility_id) REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    FOREIGN KEY (zone_id) REFERENCES storage_zones(zone_id) ON DELETE SET NULL,
    FOREIGN KEY (location_id) REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES product_categories(category_id) ON DELETE SET NULL
);

-- Count details
CREATE TABLE IF NOT EXISTS inventory_count_details (
    count_detail_id SERIAL PRIMARY KEY,
    count_id INT NOT NULL,
    item_id INT NOT NULL,
    expected_quantity NUMERIC(12,4) NOT NULL,
    counted_quantity NUMERIC(12,4) NOT NULL,
    variance NUMERIC(12,4) GENERATED ALWAYS AS (counted_quantity - expected_quantity) STORED,
    unit_of_measure VARCHAR(20) NOT NULL,
    notes TEXT,
    is_recounted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (count_id) REFERENCES inventory_counts(count_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id) ON DELETE CASCADE
);

-- Create indexes for count details
CREATE INDEX IF NOT EXISTS idx_count_details_count ON inventory_count_details(count_id);
CREATE INDEX IF NOT EXISTS idx_count_details_item ON inventory_count_details(item_id);

-- Inventory adjustments
CREATE TABLE IF NOT EXISTS inventory_adjustments (
    adjustment_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL,
    adjustment_number VARCHAR(50) NOT NULL,
    adjustment_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    adjustment_type VARCHAR(50) NOT NULL,
    reason_code VARCHAR(50),
    reference_number VARCHAR(100),
    reference_type VARCHAR(50),
    reference_id INT,
    notes TEXT,
    is_approved BOOLEAN DEFAULT FALSE,
    approved_by INT,
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE
);

-- Adjustment details
CREATE TABLE IF NOT EXISTS inventory_adjustment_details (
    adjustment_detail_id SERIAL PRIMARY KEY,
    adjustment_id INT NOT NULL,
    item_id INT NOT NULL,
    location_id INT,
    lot_number VARCHAR(100),
    serial_number VARCHAR(100),
    quantity_before NUMERIC(12,4) NOT NULL,
    quantity_after NUMERIC(12,4) NOT NULL,
    quantity_adjusted NUMERIC(12,4) GENERATED ALWAYS AS (quantity_after - quantity_before) STORED,
    unit_of_measure VARCHAR(20) NOT NULL,
    unit_cost NUMERIC(12,4),
    total_cost NUMERIC(12,4) GENERATED ALWAYS AS (quantity_adjusted * COALESCE(unit_cost, 0)) STORED,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (adjustment_id) REFERENCES inventory_adjustments(adjustment_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES storage_locations(location_id) ON DELETE SET NULL
);

-- Create indexes for adjustment details
CREATE INDEX IF NOT EXISTS idx_adj_details_adjustment ON inventory_adjustment_details(adjustment_id);
CREATE INDEX IF NOT EXISTS idx_adj_details_item ON inventory_adjustment_details(item_id);

-- Inventory reservations
CREATE TABLE IF NOT EXISTS inventory_reservations (
    reservation_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL,
    reservation_type reservation_type NOT NULL,
    status reservation_status NOT NULL DEFAULT 'PENDING',
    reference_number VARCHAR(100) NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    reference_id INT NOT NULL,
    requested_date TIMESTAMPTZ NOT NULL,
    expiry_date TIMESTAMPTZ,
    priority INT DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE
);

-- Reservation details
CREATE TABLE IF NOT EXISTS inventory_reservation_details (
    reservation_detail_id SERIAL PRIMARY KEY,
    reservation_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity_requested NUMERIC(12,4) NOT NULL,
    quantity_allocated NUMERIC(12,4) NOT NULL DEFAULT 0,
    quantity_fulfilled NUMERIC(12,4) NOT NULL DEFAULT 0,
    unit_of_measure VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (reservation_id) REFERENCES inventory_reservations(reservation_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id) ON DELETE CASCADE
);

-- Create indexes for reservation details
CREATE INDEX IF NOT EXISTS idx_resv_details_reservation ON inventory_reservation_details(reservation_id);
CREATE INDEX IF NOT EXISTS idx_resv_details_item ON inventory_reservation_details(item_id);

-- Inventory allocation details
CREATE TABLE IF NOT EXISTS inventory_allocations (
    allocation_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL,
    reservation_detail_id INT NOT NULL,
    item_id INT NOT NULL,
    location_id INT,
    lot_number VARCHAR(100),
    serial_number VARCHAR(100),
    quantity_allocated NUMERIC(12,4) NOT NULL,
    quantity_fulfilled NUMERIC(12,4) NOT NULL DEFAULT 0,
    unit_of_measure VARCHAR(20) NOT NULL,
    expiry_date DATE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    updated_by INT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_detail_id) REFERENCES inventory_reservation_details(reservation_detail_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES storage_locations(location_id) ON DELETE SET NULL
);

-- Create indexes for allocations
CREATE INDEX IF NOT EXISTS idx_allocations_reservation ON inventory_allocations(reservation_detail_id);
CREATE INDEX IF NOT EXISTS idx_allocations_item ON inventory_allocations(item_id);
CREATE INDEX IF NOT EXISTS idx_allocations_location ON inventory_allocations(location_id);

-- Create triggers for audit logging and timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers to all tables
DO $$
DECLARE
    t record;
BEGIN
    FOR t IN 
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename LIKE 'inventory_%' 
        AND tablename NOT LIKE '%_audit'
    LOOP
        EXECUTE format('DROP TRIGGER IF EXISTS set_updated_at_%s ON %I', t.tablename, t.tablename);
        EXECUTE format('CREATE TRIGGER set_updated_at_%s BEFORE UPDATE ON %I FOR EACH ROW EXECUTE FUNCTION update_updated_at_column()', 
                      t.tablename, t.tablename);
    END LOOP;
END;
$$ LANGUAGE plpgsql;
