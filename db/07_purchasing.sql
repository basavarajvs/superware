-- ===================================================
-- MODULE: PURCHASING
-- ===================================================

-- Purchase order status
CREATE TYPE po_status AS ENUM('DRAFT', 'PENDING', 'CONFIRMED', 'PARTIALLY_RECEIVED', 'COMPLETED', 'CANCELLED', 'ON_HOLD');

-- ASN (Advanced Shipping Notice) status
CREATE TYPE asn_status AS ENUM('CREATED', 'SENT', 'RECEIVED', 'PARTIALLY_RECEIVED', 'COMPLETED', 'CANCELLED');

-- Receiving status
CREATE TYPE receiving_status AS ENUM('NEW', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'PARTIALLY_RECEIVED');

-- Quality check status
CREATE TYPE qc_status AS ENUM('PENDING', 'PASSED', 'FAILED', 'CONDITIONAL_PASS');

-- Purchase orders
CREATE TABLE purchase_orders (
    po_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Vendor Information
    vendor_id INT NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    vendor_contact VARCHAR(100),
    vendor_email VARCHAR(100),
    
    -- Facility Information
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    
    -- Order Information
    order_date DATE NOT NULL,
    expected_delivery_date DATE,
    po_status po_status DEFAULT 'DRAFT',
    
    -- Financial Information
    currency_code CHAR(3) DEFAULT 'USD',
    subtotal NUMERIC(18,2) DEFAULT 0.00,
    tax_amount NUMERIC(18,2) DEFAULT 0.00,
    discount_amount NUMERIC(18,2) DEFAULT 0.00,
    shipping_amount NUMERIC(18,2) DEFAULT 0.00,
    total_amount NUMERIC(18,2) DEFAULT 0.00,
    payment_terms VARCHAR(50) DEFAULT 'NET_30',
    
    -- Status and Metadata
    notes TEXT,
    internal_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    approved_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    approved_at TIMESTAMPTZ,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_purchase_orders_updated_at
BEFORE UPDATE ON purchase_orders
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Purchase order items
CREATE TABLE purchase_order_items (
    po_item_id SERIAL PRIMARY KEY,
    po_id INT NOT NULL REFERENCES purchase_orders(po_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    
    -- Order Details
    line_number INT NOT NULL,
    quantity_ordered NUMERIC(12,4) NOT NULL,
    quantity_received NUMERIC(12,4) DEFAULT 0.00,
    quantity_outstanding NUMERIC(12,4) NOT NULL, -- Computed as (ordered - received)
    
    -- Pricing
    unit_price NUMERIC(18,4) NOT NULL,
    tax_rate NUMERIC(5,2) DEFAULT 0.00,
    tax_amount NUMERIC(18,2) DEFAULT 0.00,
    discount_rate NUMERIC(5,2) DEFAULT 0.00,
    discount_amount NUMERIC(18,2) DEFAULT 0.00,
    line_subtotal NUMERIC(18,2) NOT NULL, -- Computed as (unit_price * quantity_ordered)
    line_total NUMERIC(18,2) NOT NULL, -- Computed as (line_subtotal + tax - discount)
    
    -- Delivery Information
    expected_delivery_date DATE,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(po_id, line_number)
);

CREATE TRIGGER set_purchase_order_items_updated_at
BEFORE UPDATE ON purchase_order_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Advanced Shipping Notices (ASN)
CREATE TABLE advance_ship_notices (
    asn_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    asn_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    po_id INT REFERENCES purchase_orders(po_id) ON DELETE SET NULL,
    vendor_id INT NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    
    -- ASN Information
    estimated_arrival_time TIMESTAMPTZ,
    asn_status asn_status DEFAULT 'CREATED',
    total_weight NUMERIC(18,2) DEFAULT 0.00,
    total_volume NUMERIC(18,2) DEFAULT 0.00,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_advance_ship_notices_updated_at
BEFORE UPDATE ON advance_ship_notices
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ASN items
CREATE TABLE asn_items (
    asn_item_id SERIAL PRIMARY KEY,
    asn_id INT NOT NULL REFERENCES advance_ship_notices(asn_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_expected NUMERIC(12,4) NOT NULL,
    quantity_received NUMERIC(12,4) DEFAULT 0.00,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_asn_items_updated_at
BEFORE UPDATE ON asn_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ASN receipts
CREATE TABLE asn_receipts (
    receipt_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    asn_id INT NOT NULL REFERENCES advance_ship_notices(asn_id) ON DELETE CASCADE,
    po_id INT REFERENCES purchase_orders(po_id) ON DELETE SET NULL,
    
    -- Receipt Information
    receipt_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    received_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    total_items_received INT DEFAULT 0,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_asn_receipts_updated_at
BEFORE UPDATE ON asn_receipts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ASN receipt items
CREATE TABLE asn_receipt_items (
    receipt_item_id SERIAL PRIMARY KEY,
    receipt_id INT NOT NULL REFERENCES asn_receipts(receipt_id) ON DELETE CASCADE,
    asn_item_id INT NOT NULL REFERENCES asn_items(asn_item_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_expected NUMERIC(12,4) NOT NULL,
    quantity_received NUMERIC(12,4) NOT NULL,
    quantity_shortage NUMERIC(12,4) NOT NULL, -- Computed as (expected - received)
    quantity_overage NUMERIC(12,4) NOT NULL, -- Computed as (received - expected)
    quantity_damaged NUMERIC(12,4) DEFAULT 0.00,
    
    -- Location Information
    location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_asn_receipt_items_updated_at
BEFORE UPDATE ON asn_receipt_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Receiving
CREATE TABLE receiving (
    receiving_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    receiving_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    asn_id INT REFERENCES advance_ship_notices(asn_id) ON DELETE SET NULL,
    po_id INT REFERENCES purchase_orders(po_id) ON DELETE SET NULL,
    vendor_id INT NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    
    -- Facility Information
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    
    -- Receiving Information
    receiving_date DATE NOT NULL,
    received_by INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    receiving_status receiving_status DEFAULT 'NEW',
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_receiving_updated_at
BEFORE UPDATE ON receiving
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Receiving details
CREATE TABLE receiving_details (
    receiving_detail_id SERIAL PRIMARY KEY,
    receiving_id INT NOT NULL REFERENCES receiving(receiving_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Reference Information
    asn_item_id INT REFERENCES asn_items(asn_item_id) ON DELETE SET NULL,
    po_item_id INT REFERENCES purchase_order_items(po_item_id) ON DELETE SET NULL,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Quantity Information
    expected_qty NUMERIC(12,4) NOT NULL,
    received_qty NUMERIC(12,4) NOT NULL,
    damaged_qty NUMERIC(12,4) DEFAULT 0.00,
    shortage_qty NUMERIC(12,4) DEFAULT 0.00,
    overage_qty NUMERIC(12,4) DEFAULT 0.00,
    
    -- Location Information
    location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Dates
    production_date DATE,
    expiry_date DATE,
    
    -- Status Information
    status receiving_status DEFAULT 'COMPLETED',
    
    -- Status and Metadata
    serial_numbers JSONB, -- JSON array of serial numbers
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_receiving_details_updated_at
BEFORE UPDATE ON receiving_details
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Quality standards
CREATE TABLE quality_standards (
    standard_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    standard_name VARCHAR(100) NOT NULL,
    standard_code VARCHAR(50) UNIQUE,
    description TEXT,
    criteria JSONB, -- JSON object defining quality criteria
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_quality_standards_updated_at
BEFORE UPDATE ON quality_standards
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Quality inspection plans
CREATE TABLE quality_inspection_plans (
    plan_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    plan_name VARCHAR(100) NOT NULL,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    standard_id INT REFERENCES quality_standards(standard_id) ON DELETE SET NULL,
    sampling_method VARCHAR(50), -- 100%, AQL, etc.
    sample_size INT,
    acceptance_criteria TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_quality_inspection_plans_updated_at
BEFORE UPDATE ON quality_inspection_plans
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Quality inspections
CREATE TABLE quality_inspections (
    inspection_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    inspection_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    reference_type VARCHAR(20) NOT NULL, -- RECEIVING, INVENTORY, CUSTOMER_RETURN
    reference_id INT NOT NULL,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Inspection Information
    plan_id INT REFERENCES quality_inspection_plans(plan_id) ON DELETE SET NULL,
    inspected_by INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    inspection_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    qc_status qc_status DEFAULT 'PENDING',
    
    -- Results
    quantity_inspected NUMERIC(12,4) NOT NULL,
    quantity_passed NUMERIC(12,4) NOT NULL,
    quantity_failed NUMERIC(12,4) NOT NULL, -- Computed as (inspected - passed)
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_quality_inspections_updated_at
BEFORE UPDATE ON quality_inspections
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Quality inspection items
CREATE TABLE quality_inspection_items (
    inspection_item_id SERIAL PRIMARY KEY,
    inspection_id INT NOT NULL REFERENCES quality_inspections(inspection_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Criteria Information
    criterion_name VARCHAR(100) NOT NULL,
    expected_value TEXT,
    actual_value TEXT,
    is_passed BOOLEAN,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_quality_inspection_items_updated_at
BEFORE UPDATE ON quality_inspection_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Quality alerts
CREATE TABLE quality_alerts (
    alert_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    alert_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    inspection_id INT REFERENCES quality_inspections(inspection_id) ON DELETE SET NULL,
    product_id INT REFERENCES products(product_id) ON DELETE SET NULL,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Alert Information
    alert_type VARCHAR(50), -- DEFECT, NON_COMPLIANCE, RECALL, etc.
    severity_level VARCHAR(20), -- LOW, MEDIUM, HIGH, CRITICAL
    detected_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    assigned_to INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Status Information
    status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    due_date DATE,
    
    -- Status and Metadata
    description TEXT,
    resolution_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_quality_alerts_updated_at
BEFORE UPDATE ON quality_alerts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Corrective actions
CREATE TABLE quality_corrective_actions (
    action_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    action_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    alert_id INT REFERENCES quality_alerts(alert_id) ON DELETE SET NULL,
    inspection_id INT REFERENCES quality_inspections(inspection_id) ON DELETE SET NULL,
    
    -- Action Information
    action_description TEXT NOT NULL,
    assigned_to INT REFERENCES users(user_id) ON DELETE SET NULL,
    due_date DATE,
    
    -- Status Information
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    completion_date DATE,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_quality_corrective_actions_updated_at
BEFORE UPDATE ON quality_corrective_actions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Indexes for better performance
CREATE INDEX idx_purchase_orders_tenant ON purchase_orders(tenant_id);
CREATE INDEX idx_purchase_orders_vendor ON purchase_orders(vendor_id);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(po_status);
CREATE INDEX idx_purchase_order_items_po ON purchase_order_items(po_id);
CREATE INDEX idx_purchase_order_items_product ON purchase_order_items(product_id);
CREATE INDEX idx_advance_ship_notices_tenant ON advance_ship_notices(tenant_id);
CREATE INDEX idx_advance_ship_notices_po ON advance_ship_notices(po_id);
CREATE INDEX idx_advance_ship_notices_vendor ON advance_ship_notices(vendor_id);
CREATE INDEX idx_asn_items_asn ON asn_items(asn_id);
CREATE INDEX idx_asn_items_product ON asn_items(product_id);
CREATE INDEX idx_asn_receipts_tenant ON asn_receipts(tenant_id);
CREATE INDEX idx_asn_receipts_asn ON asn_receipts(asn_id);
CREATE INDEX idx_asn_receipt_items_receipt ON asn_receipt_items(receipt_id);
CREATE INDEX idx_receiving_tenant ON receiving(tenant_id);
CREATE INDEX idx_receiving_asn ON receiving(asn_id);
CREATE INDEX idx_receiving_po ON receiving(po_id);
CREATE INDEX idx_receiving_details_receiving ON receiving_details(receiving_id);
CREATE INDEX idx_receiving_details_product ON receiving_details(product_id);
CREATE INDEX idx_quality_standards_tenant ON quality_standards(tenant_id);
CREATE INDEX idx_quality_inspection_plans_tenant ON quality_inspection_plans(tenant_id);
CREATE INDEX idx_quality_inspections_tenant ON quality_inspections(tenant_id);
CREATE INDEX idx_quality_inspections_reference ON quality_inspections(reference_type, reference_id);
CREATE INDEX idx_quality_alerts_tenant ON quality_alerts(tenant_id);
CREATE INDEX idx_quality_alerts_status ON quality_alerts(status);
CREATE INDEX idx_quality_corrective_actions_tenant ON quality_corrective_actions(tenant_id);
CREATE INDEX idx_quality_corrective_actions_alert ON quality_corrective_actions(alert_id);