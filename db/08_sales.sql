-- ===================================================
-- MODULE: SALES
-- ===================================================

-- Sales order status
CREATE TYPE so_status AS ENUM('DRAFT', 'PENDING', 'CONFIRMED', 'PROCESSING', 'ON_HOLD', 'PARTIALLY_FULFILLED', 'FULFILLED', 'CANCELLED', 'CLOSED');

-- Shipping status
CREATE TYPE shipping_status AS ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'IN_TRANSIT', 'DELIVERED', 'RETURNED', 'EXCEPTION');

-- Return status
CREATE TYPE return_status AS ENUM('REQUESTED', 'AUTHORIZED', 'RECEIVED', 'INSPECTING', 'PROCESSING_REFUND', 'COMPLETED', 'REJECTED');

-- Payment status
CREATE TYPE payment_status AS ENUM('PENDING', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'REFUNDED', 'CANCELLED');

-- Sales orders
CREATE TABLE sales_orders (
    order_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Customer Information
    client_id INT NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
    client_contact_id INT REFERENCES client_addresses(address_id) ON DELETE SET NULL,
    
    -- Order Information
    order_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    required_date DATE,
    order_type VARCHAR(20) DEFAULT 'STANDARD', -- STANDARD, RUSH, BACKORDER, QUOTE, SAMPLE
    
    -- Billing Information
    billing_address_id INT REFERENCES client_addresses(address_id) ON DELETE SET NULL,
    payment_terms VARCHAR(50) DEFAULT 'NET_30',
    payment_status payment_status DEFAULT 'PENDING',
    currency_code CHAR(3) DEFAULT 'USD',
    
    -- Shipping Information
    shipping_address_id INT REFERENCES client_addresses(address_id) ON DELETE SET NULL,
    shipping_method VARCHAR(50),
    shipping_status shipping_status DEFAULT 'PENDING',
    
    -- Financial Information
    subtotal NUMERIC(18,2) DEFAULT 0.00,
    tax_amount NUMERIC(18,2) DEFAULT 0.00,
    discount_amount NUMERIC(18,2) DEFAULT 0.00,
    shipping_amount NUMERIC(18,2) DEFAULT 0.00,
    total_amount NUMERIC(18,2) DEFAULT 0.00, -- Computed as (subtotal + tax + shipping - discount)
    
    -- Status Information
    so_status so_status DEFAULT 'DRAFT',
    
    -- Status and Metadata
    notes TEXT,
    internal_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_sales_orders_updated_at
BEFORE UPDATE ON sales_orders
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Sales order items
CREATE TABLE sales_order_items (
    order_item_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES sales_orders(order_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Order Details
    line_number INT NOT NULL,
    quantity_ordered NUMERIC(12,4) NOT NULL,
    quantity_shipped NUMERIC(12,4) DEFAULT 0.00,
    quantity_backordered NUMERIC(12,4) NOT NULL, -- Computed as (ordered - shipped)
    
    -- Pricing
    unit_price NUMERIC(18,4) NOT NULL,
    tax_rate NUMERIC(5,2) DEFAULT 0.00,
    discount_percent NUMERIC(5,2) DEFAULT 0.00,
    discount_amount NUMERIC(18,2) NOT NULL, -- Computed as (unit_price * quantity * discount_percent/100)
    line_subtotal NUMERIC(18,2) NOT NULL, -- Computed as (unit_price * quantity)
    tax_amount NUMERIC(18,2) NOT NULL, -- Computed as ((unit_price * quantity - discount) * tax_rate/100)
    line_total NUMERIC(18,2) NOT NULL, -- Computed as (unit_price * quantity * (1 - discount_percent/100) * (1 + tax_rate/100))
    
    -- Status Information
    status so_status DEFAULT 'PENDING',
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(order_id, line_number)
);

CREATE TRIGGER set_sales_order_items_updated_at
BEFORE UPDATE ON sales_order_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Shipments
CREATE TABLE shipments (
    shipment_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    shipment_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    order_id INT NOT NULL REFERENCES sales_orders(order_id) ON DELETE CASCADE,
    
    -- Shipment Information
    shipment_date TIMESTAMPTZ,
    shipped_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Shipping Information
    carrier_name VARCHAR(100),
    service_level VARCHAR(50), -- GROUND, 2-DAY, NEXT_DAY, etc.
    tracking_number VARCHAR(100),
    shipping_status shipping_status DEFAULT 'PENDING',
    
    -- Address Information
    ship_from_address_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    ship_to_address_id INT REFERENCES client_addresses(address_id) ON DELETE SET NULL,
    
    -- Package Information
    package_count INT DEFAULT 1,
    total_weight NUMERIC(10,2), -- in kg
    total_volume NUMERIC(10,2), -- in m³
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_shipments_updated_at
BEFORE UPDATE ON shipments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Shipment items
CREATE TABLE shipment_items (
    shipment_item_id SERIAL PRIMARY KEY,
    shipment_id INT NOT NULL REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    order_item_id INT NOT NULL REFERENCES sales_order_items(order_item_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Shipping Details
    quantity_shipped NUMERIC(12,4) NOT NULL,
    
    -- Package Information
    package_id VARCHAR(100),
    package_weight NUMERIC(10,3), -- in kg
    package_dimensions VARCHAR(50), -- LxWxH in cm
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_shipment_items_updated_at
BEFORE UPDATE ON shipment_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Customer returns/return merchandise authorization (RMA)
CREATE TABLE customer_returns (
    return_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    order_id INT REFERENCES sales_orders(order_id) ON DELETE SET NULL,
    client_id INT NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
    client_contact_id INT REFERENCES client_addresses(address_id) ON DELETE SET NULL,
    
    -- Return Information
    return_date DATE NOT NULL,
    return_reason VARCHAR(50) NOT NULL, -- DAMAGED, DEFECTIVE, WRONG_ITEM, NOT_AS_DESCRIBED, NO_LONGER_NEEDED, OTHER
    return_status return_status DEFAULT 'REQUESTED',
    
    -- Financial Information
    refund_amount NUMERIC(18,2) DEFAULT 0.00,
    refund_method VARCHAR(50), -- ORIGINAL_PAYMENT, STORE_CREDIT, REPLACEMENT, OTHER
    
    -- Status and Metadata
    notes TEXT,
    internal_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_customer_returns_updated_at
BEFORE UPDATE ON customer_returns
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Return items
CREATE TABLE return_items (
    return_item_id SERIAL PRIMARY KEY,
    return_id INT NOT NULL REFERENCES customer_returns(return_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Reference Information
    order_item_id INT REFERENCES sales_order_items(order_item_id) ON DELETE SET NULL,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Return Details
    quantity_returned NUMERIC(12,4) NOT NULL,
    quantity_accepted NUMERIC(12,4) DEFAULT 0.00,
    quantity_rejected NUMERIC(12,4) DEFAULT 0.00,
    rejection_reason VARCHAR(100),
    
    -- Financial Information
    unit_price NUMERIC(18,4) NOT NULL,
    refund_amount NUMERIC(18,2) NOT NULL, -- Computed as (unit_price * quantity_accepted)
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_return_items_updated_at
BEFORE UPDATE ON return_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Packing materials
CREATE TABLE packing_materials (
    material_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    material_name VARCHAR(100) NOT NULL,
    material_code VARCHAR(50) UNIQUE,
    description TEXT,
    weight_kg NUMERIC(10,4),
    volume_m3 NUMERIC(10,4),
    cost_price NUMERIC(18,4),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_packing_materials_updated_at
BEFORE UPDATE ON packing_materials
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Packing stations
CREATE TABLE packing_stations (
    station_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    station_name VARCHAR(100) NOT NULL,
    station_code VARCHAR(50) UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_packing_stations_updated_at
BEFORE UPDATE ON packing_stations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Packing slips
CREATE TABLE packing_slips (
    packing_slip_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    packing_slip_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    shipment_id INT NOT NULL REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    
    -- Packing Information
    packed_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    packed_at TIMESTAMPTZ,
    station_id INT REFERENCES packing_stations(station_id) ON DELETE SET NULL,
    
    -- Package Information
    package_count INT DEFAULT 1,
    total_weight NUMERIC(10,2), -- in kg
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_packing_slips_updated_at
BEFORE UPDATE ON packing_slips
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Packing slip items
CREATE TABLE packing_slip_items (
    packing_slip_item_id SERIAL PRIMARY KEY,
    packing_slip_id INT NOT NULL REFERENCES packing_slips(packing_slip_id) ON DELETE CASCADE,
    shipment_item_id INT NOT NULL REFERENCES shipment_items(shipment_item_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Packing Details
    quantity_packed NUMERIC(12,4) NOT NULL,
    package_id VARCHAR(100),
    
    -- Packing Materials
    material_id INT REFERENCES packing_materials(material_id) ON DELETE SET NULL,
    material_quantity INT DEFAULT 1,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_packing_slip_items_updated_at
BEFORE UPDATE ON packing_slip_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Indexes for better performance
CREATE INDEX idx_sales_orders_tenant ON sales_orders(tenant_id);
CREATE INDEX idx_sales_orders_client ON sales_orders(client_id);
CREATE INDEX idx_sales_orders_status ON sales_orders(so_status);
CREATE INDEX idx_sales_orders_date ON sales_orders(order_date);
CREATE INDEX idx_sales_order_items_order ON sales_order_items(order_id);
CREATE INDEX idx_sales_order_items_product ON sales_order_items(product_id);
CREATE INDEX idx_shipments_tenant ON shipments(tenant_id);
CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_status ON shipments(shipping_status);
CREATE INDEX idx_shipment_items_shipment ON shipment_items(shipment_id);
CREATE INDEX idx_shipment_items_order_item ON shipment_items(order_item_id);
CREATE INDEX idx_customer_returns_tenant ON customer_returns(tenant_id);
CREATE INDEX idx_customer_returns_client ON customer_returns(client_id);
CREATE INDEX idx_customer_returns_order ON customer_returns(order_id);
CREATE INDEX idx_customer_returns_status ON customer_returns(return_status);
CREATE INDEX idx_return_items_return ON return_items(return_id);
CREATE INDEX idx_return_items_order_item ON return_items(order_item_id);
CREATE INDEX idx_packing_materials_tenant ON packing_materials(tenant_id);
CREATE INDEX idx_packing_stations_tenant ON packing_stations(tenant_id);
CREATE INDEX idx_packing_stations_facility ON packing_stations(facility_id);
CREATE INDEX idx_packing_slips_tenant ON packing_slips(tenant_id);
CREATE INDEX idx_packing_slips_shipment ON packing_slips(shipment_id);
CREATE INDEX idx_packing_slip_items_packing_slip ON packing_slip_items(packing_slip_id);