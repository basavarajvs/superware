-- ===================================================
-- MODULE: INVENTORY
-- ===================================================

-- Inventory lot status
CREATE TYPE lot_status AS ENUM('ACTIVE', 'QUARANTINED', 'EXPIRED', 'DISPOSED');

-- Inventory item status
CREATE TYPE inventory_item_status AS ENUM('AVAILABLE', 'ALLOCATED', 'QUARANTINED', 'DAMAGED', 'SHRINKAGE', 'IN_TRANSIT', 'RESERVED');

-- Condition codes
CREATE TYPE condition_code AS ENUM('NEW', 'USED', 'REFURBISHED', 'DAMAGED', 'DEFECTIVE');

-- Transaction types
CREATE TYPE transaction_type AS ENUM('RECEIPT', 'ISSUE', 'ADJUSTMENT', 'TRANSFER', 'RETURN', 'PHYSICAL_COUNT', 'CYCLE_COUNT', 'WRITE_OFF', 'RESERVATION', 'ALLOCATION', 'DEALLOCATION');

-- Transaction status
CREATE TYPE transaction_status AS ENUM('PENDING', 'COMPLETED', 'CANCELLED', 'ERROR');

-- Count types
CREATE TYPE count_type AS ENUM('CYCLE_COUNT', 'PHYSICAL_INVENTORY', 'SPOT_CHECK');

-- Count status
CREATE TYPE count_status AS ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

-- Reservation types
CREATE TYPE reservation_type AS ENUM('SALES_ORDER', 'TRANSFER_ORDER', 'QUALITY_CHECK', 'WORK_ORDER', 'OTHER');

-- Reservation status
CREATE TYPE reservation_status AS ENUM('PENDING', 'ALLOCATED', 'PARTIALLY_ALLOCATED', 'CANCELLED', 'FULFILLED');

-- Inventory policies
CREATE TABLE inventory_policies (
    policy_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    product_id INT REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    
    -- Inventory Control
    min_stock_level NUMERIC(12,4) DEFAULT 0,
    max_stock_level NUMERIC(12,4) DEFAULT 0,
    reorder_point NUMERIC(12,4) DEFAULT 0,
    reorder_quantity NUMERIC(12,4) DEFAULT 0,
    
    -- Valuation Method
    valuation_method VARCHAR(20) DEFAULT 'FIFO', -- FIFO, LIFO, WEIGHTED_AVERAGE, SPECIFIC
    
    -- ABC Classification
    abc_class CHAR(1) DEFAULT 'C', -- A, B, C
    
    -- Storage Requirements
    preferred_zone_type zone_type,
    requires_temperature_control BOOLEAN DEFAULT FALSE,
    temperature_min NUMERIC(5,2),
    temperature_max NUMERIC(5,2),
    is_hazardous_storage BOOLEAN DEFAULT FALSE,
    
    -- Handling Instructions
    is_fragile BOOLEAN DEFAULT FALSE,
    is_stackable BOOLEAN DEFAULT TRUE,
    max_stack_height INT DEFAULT 10,
    
    -- Expiry Management
    shelf_life_days INT,
    expiry_policy VARCHAR(20) DEFAULT 'FIFO', -- FIFO, FEFO (First Expired First Out)
    
    -- Status and Metadata
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(tenant_id, product_id, variant_id)
);

CREATE TRIGGER set_inventory_policies_updated_at
BEFORE UPDATE ON inventory_policies
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Inventory lots/batches
CREATE TABLE inventory_lots (
    lot_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    lot_number VARCHAR(100) NOT NULL,
    
    -- Lot Information
    supplier_lot_number VARCHAR(100),
    manufacture_date DATE,
    expiration_date DATE,
    best_before_date DATE,
    
    -- Status Information
    status lot_status DEFAULT 'ACTIVE',
    is_quarantined BOOLEAN DEFAULT FALSE,
    quarantine_reason TEXT,
    
    -- Status and Metadata
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(tenant_id, product_id, lot_number)
);

CREATE TRIGGER set_inventory_lots_updated_at
BEFORE UPDATE ON inventory_lots
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Inventory items (physical instances for serialized inventory)
CREATE TABLE inventory_items (
    item_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Identification
    serial_number VARCHAR(100),
    barcode VARCHAR(50),
    
    -- Status Information
    status inventory_item_status DEFAULT 'AVAILABLE',
    condition_code condition_code DEFAULT 'NEW',
    
    -- Location Information
    facility_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Status and Metadata
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(tenant_id, product_id, serial_number)
);

CREATE TRIGGER set_inventory_items_updated_at
BEFORE UPDATE ON inventory_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Inventory on-hand quantities
CREATE TABLE inventory_on_hand (
    on_hand_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    location_id INT REFERENCES storage_locations(location_id) ON DELETE CASCADE,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_on_hand NUMERIC(12,4) NOT NULL DEFAULT 0,
    quantity_allocated NUMERIC(12,4) NOT NULL DEFAULT 0,
    quantity_available NUMERIC(12,4) NOT NULL DEFAULT 0, -- Computed field for available quantity
    
    -- Status and Metadata
    last_count_date TIMESTAMPTZ,
    next_count_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, product_id, variant_id, facility_id, location_id, lot_id)
);

CREATE TRIGGER set_inventory_on_hand_updated_at
BEFORE UPDATE ON inventory_on_hand
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Inventory movements/transactions
CREATE TABLE inventory_transactions (
    transaction_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Transaction Information
    transaction_type transaction_type NOT NULL,
    transaction_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    reference_number VARCHAR(50),
    reference_type VARCHAR(20),
    reference_id INT,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    item_id INT REFERENCES inventory_items(item_id) ON DELETE SET NULL,
    
    -- Location Information
    from_facility_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    from_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    to_facility_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    to_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity NUMERIC(12,4) NOT NULL,
    unit_cost NUMERIC(12,4),
    total_cost NUMERIC(12,4), -- Computed as quantity * unit_cost
    
    -- Status Information
    status transaction_status DEFAULT 'COMPLETED',
    reason_code VARCHAR(50),
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_inventory_transactions_updated_at
BEFORE UPDATE ON inventory_transactions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Inventory counts and adjustments
CREATE TABLE inventory_counts (
    count_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Count Information
    count_number VARCHAR(50) NOT NULL,
    count_type count_type NOT NULL,
    count_status count_status DEFAULT 'PENDING',
    
    -- Scope
    facility_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    zone_id INT REFERENCES storage_zones(zone_id) ON DELETE SET NULL,
    location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Timing
    scheduled_date DATE,
    start_time TIMESTAMPTZ,
    complete_time TIMESTAMPTZ,
    
    -- Results
    total_items_counted INT DEFAULT 0,
    total_variance_value NUMERIC(12,2) DEFAULT 0,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    completed_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_inventory_counts_updated_at
BEFORE UPDATE ON inventory_counts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Inventory count details
CREATE TABLE inventory_count_items (
    count_item_id SERIAL PRIMARY KEY,
    count_id INT NOT NULL REFERENCES inventory_counts(count_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Item Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Quantities
    expected_quantity NUMERIC(12,4) DEFAULT 0,
    counted_quantity NUMERIC(12,4) DEFAULT 0,
    variance_quantity NUMERIC(12,4) DEFAULT 0, -- Computed as (counted - expected)
    
    -- Status and Metadata
    is_recount BOOLEAN DEFAULT FALSE,
    notes TEXT,
    counted_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    counted_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(count_id, product_id, variant_id, lot_id, location_id)
);

-- Inventory reservations
CREATE TABLE inventory_reservations (
    reservation_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Reservation Information
    reservation_type reservation_type NOT NULL,
    reference_number VARCHAR(50) NOT NULL,
    reference_type VARCHAR(20) NOT NULL,
    reference_id INT NOT NULL,
    
    -- Item Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Location Information
    facility_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_reserved NUMERIC(12,4) NOT NULL,
    quantity_allocated NUMERIC(12,4) DEFAULT 0,
    
    -- Status Information
    status reservation_status DEFAULT 'PENDING',
    expiration_date TIMESTAMPTZ,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_inventory_reservations_updated_at
BEFORE UPDATE ON inventory_reservations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Inventory adjustments
CREATE TABLE inventory_adjustments (
    adjustment_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Adjustment Information
    adjustment_number VARCHAR(50) NOT NULL UNIQUE,
    adjustment_reason VARCHAR(100),
    adjustment_type VARCHAR(50), -- CYCLE_COUNT, DAMAGE, THEFT, OBSOLETE, etc.
    
    -- Item Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Location Information
    facility_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_before NUMERIC(12,4) NOT NULL,
    quantity_adjusted NUMERIC(12,4) NOT NULL, -- Positive for increase, negative for decrease
    quantity_after NUMERIC(12,4) NOT NULL,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_inventory_adjustments_updated_at
BEFORE UPDATE ON inventory_adjustments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Indexes for better performance
CREATE INDEX idx_inventory_policies_tenant ON inventory_policies(tenant_id);
CREATE INDEX idx_inventory_policies_product ON inventory_policies(product_id);
CREATE INDEX idx_inventory_lots_tenant ON inventory_lots(tenant_id);
CREATE INDEX idx_inventory_lots_product ON inventory_lots(product_id);
CREATE INDEX idx_inventory_items_tenant ON inventory_items(tenant_id);
CREATE INDEX idx_inventory_items_product ON inventory_items(product_id);
CREATE INDEX idx_inventory_items_serial ON inventory_items(serial_number);
CREATE INDEX idx_inventory_on_hand_tenant ON inventory_on_hand(tenant_id);
CREATE INDEX idx_inventory_on_hand_product ON inventory_on_hand(product_id);
CREATE INDEX idx_inventory_on_hand_location ON inventory_on_hand(facility_id, location_id);
CREATE INDEX idx_inventory_transactions_tenant ON inventory_transactions(tenant_id);
CREATE INDEX idx_inventory_transactions_product ON inventory_transactions(product_id);
CREATE INDEX idx_inventory_transactions_date ON inventory_transactions(transaction_date);
CREATE INDEX idx_inventory_transactions_reference ON inventory_transactions(reference_type, reference_id);
CREATE INDEX idx_inventory_counts_tenant ON inventory_counts(tenant_id);
CREATE INDEX idx_inventory_counts_status ON inventory_counts(count_status);
CREATE INDEX idx_inventory_count_items_count ON inventory_count_items(count_id);
CREATE INDEX idx_inventory_reservations_tenant ON inventory_reservations(tenant_id);
CREATE INDEX idx_inventory_reservations_reference ON inventory_reservations(reference_type, reference_id);
CREATE INDEX idx_inventory_reservations_status ON inventory_reservations(status);
CREATE INDEX idx_inventory_adjustments_tenant ON inventory_adjustments(tenant_id);
CREATE INDEX idx_inventory_adjustments_product ON inventory_adjustments(product_id);