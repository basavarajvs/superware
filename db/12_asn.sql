-- ===================================================
-- MODULE: ADVANCED ASN MANAGEMENT
-- ===================================================
-- Enhanced ASN management with complete lifecycle tracking

-- ASN Status Types
CREATE TYPE asn_status AS ENUM (
    'DRAFT',
    'SENT',
    'IN_TRANSIT',
    'PARTIALLY_RECEIVED',
    'RECEIVED',
    'CANCELLED',
    'DELAYED',
    'EXCEPTION'
);

-- ASN Master Table
CREATE TABLE advance_ship_notices (
    asn_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    asn_number VARCHAR(50) NOT NULL,
    po_id INT REFERENCES purchase_orders(po_id) ON DELETE SET NULL,
    vendor_id INT NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    status asn_status NOT NULL DEFAULT 'DRAFT',
    
    -- Shipment Details
    carrier_name VARCHAR(100),
    tracking_number VARCHAR(100),
    bill_of_lading VARCHAR(100),
    
    -- Dates
    expected_delivery_date TIMESTAMPTZ,
    actual_delivery_date TIMESTAMPTZ,
    ship_date TIMESTAMPTZ,
    
    -- Measurements
    total_weight_kg DECIMAL(10,3),
    total_volume_cbm DECIMAL(10,3),
    total_units INT NOT NULL DEFAULT 0,
    total_cartons INT,
    
    -- References
    facility_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    
    -- Audit Fields
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    UNIQUE(tenant_id, asn_number)
);

-- ASN Items Table
CREATE TABLE asn_items (
    asn_item_id SERIAL PRIMARY KEY,
    asn_id INT NOT NULL REFERENCES advance_ship_notices(asn_id) ON DELETE CASCADE,
    po_item_id INT REFERENCES purchase_order_items(po_item_id) ON DELETE SET NULL,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    
    -- Quantities
    expected_quantity NUMERIC(12,4) NOT NULL,
    received_quantity NUMERIC(12,4) DEFAULT 0,
    damaged_quantity NUMERIC(12,4) DEFAULT 0,
    rejected_quantity NUMERIC(12,4) DEFAULT 0,
    
    -- Product Details (denormalized for performance)
    sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    
    -- Lot/Batch Info
    lot_number VARCHAR(100),
    manufacture_date DATE,
    expiration_date DATE,
    
    -- Pricing (optional, for verification)
    unit_cost DECIMAL(12,4),
    currency VARCHAR(3),
    
    -- Status
    is_inspected BOOLEAN DEFAULT FALSE,
    inspection_status VARCHAR(50),
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CHECK (received_quantity + damaged_quantity + rejected_quantity <= expected_quantity)
);

-- ASN Receipts (for tracking multiple receipts against an ASN)
CREATE TABLE asn_receipts (
    receipt_id SERIAL PRIMARY KEY,
    asn_id INT NOT NULL REFERENCES advance_ship_notices(asn_id) ON DELETE CASCADE,
    receipt_number VARCHAR(50) NOT NULL,
    receipt_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Receiving Details
    received_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    receiving_notes TEXT,
    
    -- Status
    is_complete BOOLEAN DEFAULT FALSE,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    UNIQUE(tenant_id, receipt_number)
);

-- ASN Receipt Items
CREATE TABLE asn_receipt_items (
    receipt_item_id SERIAL PRIMARY KEY,
    receipt_id INT NOT NULL REFERENCES asn_receipts(receipt_id) ON DELETE CASCADE,
    asn_item_id INT NOT NULL REFERENCES asn_items(asn_item_id) ON DELETE CASCADE,
    
    -- Quantities
    quantity_received NUMERIC(12,4) NOT NULL,
    quantity_damaged NUMERIC(12,4) DEFAULT 0,
    quantity_rejected NUMERIC(12,4) DEFAULT 0,
    
    -- Location
    location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Quality Check
    passed_quality_check BOOLEAN,
    quality_notes TEXT,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ASN Exceptions (for tracking any discrepancies or issues)
CREATE TABLE asn_exceptions (
    exception_id SERIAL PRIMARY KEY,
    asn_id INT NOT NULL REFERENCES advance_ship_notices(asn_id) ON DELETE CASCADE,
    asn_item_id INT REFERENCES asn_items(asn_item_id) ON DELETE CASCADE,
    
    -- Exception Details
    exception_type VARCHAR(50) NOT NULL, -- 'DAMAGED', 'QUANTITY_MISMATCH', 'WRONG_ITEM', etc.
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'IN_REVIEW', 'RESOLVED', 'REJECTED'
    
    -- Resolution
    resolution_notes TEXT,
    resolved_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    resolved_at TIMESTAMPTZ,
    
    -- Audit Fields
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better query performance
CREATE INDEX idx_asn_tenant ON advance_ship_notices(tenant_id);
CREATE INDEX idx_asn_number ON advance_ship_notices(asn_number);
CREATE INDEX idx_asn_status ON advance_ship_notices(status);
CREATE INDEX idx_asn_po ON advance_ship_notices(po_id);
CREATE INDEX idx_asn_vendor ON advance_ship_notices(vendor_id);
CREATE INDEX idx_asn_expected_delivery ON advance_ship_notices(expected_delivery_date);

CREATE INDEX idx_asn_items_asn ON asn_items(asn_id);
CREATE INDEX idx_asn_items_po_item ON asn_items(po_item_id);
CREATE INDEX idx_asn_items_product ON asn_items(product_id);

-- Triggers for maintaining data integrity
CREATE OR REPLACE FUNCTION update_asn_status()
RETURNS TRIGGER AS $$
BEGIN
    -- Update ASN status based on received quantities
    UPDATE advance_ship_notices asn
    SET 
        status = CASE 
            WHEN NOT EXISTS (SELECT 1 FROM asn_items WHERE asn_id = NEW.asn_id) THEN 'DRAFT'
            WHEN NOT EXISTS (SELECT 1 FROM asn_items WHERE asn_id = NEW.asn_id AND received_quantity > 0) THEN 'SENT'
            WHEN EXISTS (SELECT 1 FROM asn_items WHERE asn_id = NEW.asn_id AND received_quantity < expected_quantity) THEN 'PARTIALLY_RECEIVED'
            ELSE 'RECEIVED'
        END,
        updated_at = CURRENT_TIMESTAMP
    WHERE asn_id = NEW.asn_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_asn_items_after
AFTER INSERT OR UPDATE ON asn_items
FOR EACH ROW
EXECUTE FUNCTION update_asn_status();

-- Function to create ASN from PO
CREATE OR REPLACE FUNCTION create_asn_from_po(
    p_po_id INT,
    p_vendor_id INT,
    p_expected_delivery TIMESTAMPTZ,
    p_created_by INT
) 
RETURNS INT AS $$
DECLARE
    v_tenant_id INT;
    v_asn_id INT;
    v_asn_number VARCHAR(50);
BEGIN
    -- Get tenant_id from PO
    SELECT tenant_id INTO v_tenant_id 
    FROM purchase_orders 
    WHERE po_id = p_po_id;
    
    IF v_tenant_id IS NULL THEN
        RAISE EXCEPTION 'Purchase Order not found';
    END IF;
    
    -- Generate ASN number
    SELECT 'ASN-' || to_char(CURRENT_DATE, 'YYYYMM') || '-' || 
           lpad((COALESCE(MAX(SUBSTRING(asn_number, 'ASN-\d{6}-(\d+)')::INT), 0) + 1)::TEXT, 5, '0')
    INTO v_asn_number
    FROM advance_ship_notices
    WHERE asn_number ~ ('^ASN-' || to_char(CURRENT_DATE, 'YYYYMM') || '-\d+$');
    
    -- Create ASN
    INSERT INTO advance_ship_notices (
        tenant_id, asn_number, po_id, vendor_id, 
        expected_delivery_date, status, created_by
    ) VALUES (
        v_tenant_id, v_asn_number, p_po_id, p_vendor_id,
        p_expected_delivery, 'DRAFT', p_created_by
    )
    RETURNING asn_id INTO v_asn_id;
    
    -- Add PO items to ASN
    INSERT INTO asn_items (
        asn_id, po_item_id, product_id, variant_id,
        expected_quantity, sku, product_name,
        unit_cost, currency
    )
    SELECT 
        v_asn_id, poi.po_item_id, poi.product_id, poi.variant_id,
        poi.quantity_ordered, p.product_code, p.product_name,
        poi.unit_price, po.currency
    FROM purchase_order_items poi
    JOIN products p ON p.product_id = poi.product_id
    JOIN purchase_orders po ON po.po_id = poi.po_id
    WHERE poi.po_id = p_po_id
    AND poi.quantity_ordered > COALESCE(
        (SELECT SUM(ai.expected_quantity) 
         FROM asn_items ai 
         JOIN advance_ship_notices an ON an.asn_id = ai.asn_id
         WHERE ai.po_item_id = poi.po_item_id 
         AND an.status != 'CANCELLED'), 0);
    
    -- Update ASN status based on items
    PERFORM update_asn_status();
    
    RETURN v_asn_id;
END;
$$ LANGUAGE plpgsql;

-- Function to receive ASN items
CREATE OR REPLACE FUNCTION receive_asn_items(
    p_asn_id INT,
    p_receipt_number VARCHAR(50),
    p_received_by INT,
    p_receiving_notes TEXT DEFAULT NULL,
    p_receipt_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
)
RETURNS INT AS $$
DECLARE
    v_receipt_id INT;
    v_tenant_id INT;
BEGIN
    -- Get tenant_id from ASN
    SELECT tenant_id INTO v_tenant_id 
    FROM advance_ship_notices 
    WHERE asn_id = p_asn_id;
    
    IF v_tenant_id IS NULL THEN
        RAISE EXCEPTION 'ASN not found';
    END IF;
    
    -- Create receipt header
    INSERT INTO asn_receipts (
        asn_id, receipt_number, receipt_date,
        received_by, receiving_notes, is_complete
    ) VALUES (
        p_asn_id, p_receipt_number, p_receipt_date,
        p_received_by, p_receiving_notes, FALSE
    )
    RETURNING receipt_id INTO v_receipt_id;
    
    -- Mark receipt as complete if all items are received
    UPDATE asn_receipts
    SET is_complete = TRUE
    WHERE receipt_id = v_receipt_id
    AND NOT EXISTS (
        SELECT 1 FROM asn_items ai
        WHERE ai.asn_id = p_asn_id
        AND ai.received_quantity < ai.expected_quantity
    );
    
    RETURN v_receipt_id;
END;
$$ LANGUAGE plpgsql;

-- Function to add receipt items
CREATE OR REPLACE FUNCTION add_receipt_item(
    p_receipt_id INT,
    p_asn_item_id INT,
    p_quantity_received NUMERIC(12,4),
    p_quantity_damaged NUMERIC(12,4) DEFAULT 0,
    p_quantity_rejected NUMERIC(12,4) DEFAULT 0,
    p_location_id INT DEFAULT NULL,
    p_passed_quality_check BOOLEAN DEFAULT NULL,
    p_quality_notes TEXT DEFAULT NULL
)
RETURNS VOID AS $$
DECLARE
    v_asn_id INT;
    v_expected_quantity NUMERIC(12,4);
    v_total_received NUMERIC(12,4);
    v_tenant_id INT;
BEGIN
    -- Get ASN ID and validate
    SELECT ai.asn_id, ai.expected_quantity, an.tenant_id
    INTO v_asn_id, v_expected_quantity, v_tenant_id
    FROM asn_items ai
    JOIN advance_ship_notices an ON an.asn_id = ai.asn_item_id
    JOIN asn_receipts ar ON ar.asn_id = an.asn_id
    WHERE ai.asn_item_id = p_asn_item_id
    AND ar.receipt_id = p_receipt_id;
    
    IF v_asn_id IS NULL THEN
        RAISE EXCEPTION 'ASN item or receipt not found';
    END IF;
    
    -- Get total received so far (including this receipt)
    SELECT COALESCE(SUM(ari.quantity_received), 0)
    INTO v_total_received
    FROM asn_receipt_items ari
    JOIN asn_receipts ar ON ar.receipt_id = ari.receipt_id
    WHERE ari.asn_item_id = p_asn_item_id
    AND ar.receipt_id != p_receipt_id;
    
    -- Validate quantities
    IF (v_total_received + p_quantity_received + p_quantity_damaged + p_quantity_rejected) > v_expected_quantity THEN
        RAISE EXCEPTION 'Total received quantity exceeds expected quantity';
    END IF;
    
    -- Add receipt item
    INSERT INTO asn_receipt_items (
        receipt_id, asn_item_id, quantity_received,
        quantity_damaged, quantity_rejected, location_id,
        passed_quality_check, quality_notes
    ) VALUES (
        p_receipt_id, p_asn_item_id, p_quantity_received,
        p_quantity_damaged, p_quantity_rejected, p_location_id,
        p_passed_quality_check, p_quality_notes
    );
    
    -- Update ASN item with total received quantities
    UPDATE asn_items
    SET 
        received_quantity = received_quantity + p_quantity_received,
        damaged_quantity = damaged_quantity + p_quantity_damaged,
        rejected_quantity = rejected_quantity + p_quantity_rejected,
        updated_at = CURRENT_TIMESTAMP
    WHERE asn_item_id = p_asn_item_id;
    
    -- Update inventory if location is provided and item passed quality check
    IF p_location_id IS NOT NULL AND (p_passed_quality_check IS NULL OR p_passed_quality_check = TRUE) THEN
        PERFORM add_inventory_transaction(
            v_tenant_id,
            'RECEIVING',
            p_quantity_received,
            NULL, -- from_location_id
            p_location_id,
            p_asn_item_id, -- reference_id
            'ASN_ITEM', -- reference_type
            NULL, -- lot_id (will be created if needed)
            p_quantity_received * (
                SELECT unit_cost 
                FROM asn_items 
                WHERE asn_item_id = p_asn_item_id
            ), -- unit_cost
            NULL -- notes
        );
    END IF;
    
    -- Update receipt completion status
    IF NOT EXISTS (
        SELECT 1 FROM asn_items ai
        WHERE ai.asn_id = v_asn_id
        AND (ai.received_quantity + ai.damaged_quantity + ai.rejected_quantity) < ai.expected_quantity
    ) THEN
        UPDATE asn_receipts
        SET is_complete = TRUE,
            updated_at = CURRENT_TIMESTAMP
        WHERE receipt_id = p_receipt_id;
        
        -- Update ASN status to RECEIVED if all items are received
        UPDATE advance_ship_notices
        SET status = 'RECEIVED',
            actual_delivery_date = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE asn_id = v_asn_id
        AND status != 'CANCELLED';
    END IF;
END;
$$ LANGUAGE plpgsql;