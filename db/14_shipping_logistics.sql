-- ===================================================
-- MODULE: SHIPPING & LOGISTICS
-- ===================================================
-- Complete shipping and logistics management

-- Shipment Status Types
CREATE TYPE shipment_status AS ENUM (
    'DRAFT',
    'PLANNED',
    'PICKING',
    'PICKED',
    'PACKING',
    'PACKED',
    'AWAITING_SHIPMENT',
    'IN_TRANSIT',
    'OUT_FOR_DELIVERY',
    'DELIVERED',
    'EXCEPTION',
    'CANCELLED'
);

-- Package Types
CREATE TYPE package_type AS ENUM (
    'CARTON',
    'PALLET',
    'ENVELOPE',
    'TOTE',
    'CUSTOM'
);

-- Carriers
CREATE TABLE carriers (
    carrier_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    carrier_code VARCHAR(50) NOT NULL,
    carrier_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    default_service_level VARCHAR(50),
    tracking_url_template VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    UNIQUE(tenant_id, carrier_code)
);

-- Carrier Services
CREATE TABLE carrier_services (
    service_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    carrier_id INT NOT NULL REFERENCES carriers(carrier_id) ON DELETE CASCADE,
    
    -- Service Details
    service_code VARCHAR(50) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- Service Attributes
    is_domestic BOOLEAN DEFAULT TRUE,
    is_international BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Transit Time
    min_transit_days INT,
    max_transit_days INT,
    
    -- Dimensional Weight
    uses_dimensional_weight BOOLEAN DEFAULT FALSE,
    dimensional_factor DECIMAL(10,4),
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, carrier_id, service_code)
);

-- Shipments
CREATE TABLE shipments (
    shipment_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    shipment_number VARCHAR(50) NOT NULL,
    status shipment_status NOT NULL DEFAULT 'DRAFT',
    
    -- Order Information
    order_id INT, -- References sales_orders(order_id)
    order_number VARCHAR(50),
    
    -- Ship From
    origin_facility_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    
    -- Ship To
    client_id INT REFERENCES clients(client_id) ON DELETE SET NULL,
    ship_to_address_id INT, -- Can reference client_addresses or be standalone
    
    -- Shipping Details
    carrier_id INT REFERENCES carriers(carrier_id) ON DELETE SET NULL,
    carrier_service_id INT REFERENCES carrier_services(service_id) ON DELETE SET NULL,
    tracking_number VARCHAR(100),
    shipping_method VARCHAR(50),
    
    -- Dates
    ship_date DATE,
    estimated_delivery_date DATE,
    actual_delivery_date TIMESTAMPTZ,
    
    -- Package Information
    package_count INT DEFAULT 0,
    total_weight_kg DECIMAL(10,3),
    total_volume_cbm DECIMAL(10,3),
    
    -- Financial
    shipping_cost DECIMAL(12,4),
    currency VARCHAR(3) DEFAULT 'USD',
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, shipment_number)
);

-- Packages
CREATE TABLE packages (
    package_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    package_number VARCHAR(50) NOT NULL,
    package_type package_type NOT NULL,
    
    -- Dimensions
    length_cm DECIMAL(8,2),
    width_cm DECIMAL(8,2),
    height_cm DECIMAL(8,2),
    weight_kg DECIMAL(10,3) NOT NULL,
    volume_cbm DECIMAL(10,6) GENERATED ALWAYS AS 
        (COALESCE(length_cm, 0) * COALESCE(width_cm, 0) * COALESCE(height_cm, 0) / 1000000) STORED,
    
    -- Contents
    item_count INT DEFAULT 0,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    
    -- Shipment
    shipment_id INT REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    
    -- Tracking
    tracking_number VARCHAR(100),
    barcode VARCHAR(100),
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, package_number)
);

-- Package Items
CREATE TABLE package_items (
    package_item_id SERIAL PRIMARY KEY,
    package_id INT NOT NULL REFERENCES packages(package_id) ON DELETE CASCADE,
    
    -- Item Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    quantity NUMERIC(12,4) NOT NULL,
    
    -- Serial/Lot Numbers
    serial_numbers TEXT[],
    lot_number VARCHAR(100),
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PACKED',
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL
);

-- Shipping Labels
CREATE TABLE shipping_labels (
    label_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Reference
    shipment_id INT REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    package_id INT REFERENCES packages(package_id) ON DELETE CASCADE,
    
    -- Label Details
    label_format VARCHAR(20) NOT NULL, -- 'PDF', 'ZPL', 'EPL', 'PNG', etc.
    label_data BYTEA,
    label_url VARCHAR(255),
    label_checksum VARCHAR(64),
    
    -- Carrier Information
    carrier_id INT REFERENCES carriers(carrier_id) ON DELETE SET NULL,
    carrier_tracking_number VARCHAR(100),
    carrier_service_code VARCHAR(50),
    
    -- Label Metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    CHECK (shipment_id IS NOT NULL OR package_id IS NOT NULL)
);

-- Shipping Rates
CREATE TABLE shipping_rates (
    rate_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    carrier_service_id INT NOT NULL REFERENCES carrier_services(service_id) ON DELETE CASCADE,
    
    -- Rate Details
    origin_country VARCHAR(2) NOT NULL,
    origin_postal_code VARCHAR(20),
    destination_country VARCHAR(2) NOT NULL,
    destination_postal_code VARCHAR(20),
    
    -- Weight and Dimensions
    min_weight_kg DECIMAL(10,3) NOT NULL,
    max_weight_kg DECIMAL(10,3) NOT NULL,
    
    -- Rate Information
    base_rate DECIMAL(12,4) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    
    -- Effective Dates
    effective_from DATE NOT NULL,
    effective_to DATE,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    CHECK (max_weight_kg > min_weight_kg),
    CHECK (effective_to IS NULL OR effective_to > effective_from)
);

-- Shipment Tracking
CREATE TABLE shipment_tracking (
    tracking_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Reference
    shipment_id INT REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    package_id INT REFERENCES packages(package_id) ON DELETE CASCADE,
    
    -- Tracking Information
    tracking_number VARCHAR(100) NOT NULL,
    carrier_id INT REFERENCES carriers(carrier_id) ON DELETE SET NULL,
    
    -- Status
    status VARCHAR(50) NOT NULL,
    status_description TEXT,
    status_date TIMESTAMPTZ NOT NULL,
    
    -- Location
    location_name VARCHAR(100),
    city VARCHAR(100),
    state_province VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(2),
    
    -- Additional Data
    details JSONB,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes for faster lookups
    INDEX idx_tracking_number (tenant_id, tracking_number)
);

-- Audit Triggers
CREATE TRIGGER set_carriers_updated_at
BEFORE UPDATE ON carriers
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_carrier_services_updated_at
BEFORE UPDATE ON carrier_services
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_shipments_updated_at
BEFORE UPDATE ON shipments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_packages_updated_at
BEFORE UPDATE ON packages
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_shipping_rates_updated_at
BEFORE UPDATE ON shipping_rates
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Function to calculate shipping cost
CREATE OR REPLACE FUNCTION calculate_shipping_cost(
    p_tenant_id INT,
    p_carrier_service_id INT,
    p_weight_kg DECIMAL,
    p_volume_cbm DECIMAL,
    p_origin_country VARCHAR(2),
    p_origin_postal_code VARCHAR(20),
    p_dest_country VARCHAR(2),
    p_dest_postal_code VARCHAR(20)
)
RETURNS DECIMAL(12,4) AS $$
DECLARE
    v_rate DECIMAL(12,4);
    v_dimensional_weight DECIMAL(12,4);
    v_chargeable_weight DECIMAL(12,4);
    v_uses_dimensional_weight BOOLEAN;
    v_dimensional_factor DECIMAL(10,4);
BEGIN
    -- Get carrier service details
    SELECT uses_dimensional_weight, dimensional_factor
    INTO v_uses_dimensional_weight, v_dimensional_factor
    FROM carrier_services
    WHERE service_id = p_carrier_service_id
    AND tenant_id = p_tenant_id
    AND is_active = TRUE;
    
    -- Calculate dimensional weight if applicable
    IF v_uses_dimensional_weight AND p_volume_cbm > 0 AND v_dimensional_factor > 0 THEN
        v_dimensional_weight := p_volume_cbm * v_dimensional_factor;
    ELSE
        v_dimensional_weight := 0;
    END IF;
    
    -- Determine chargeable weight
    IF v_dimensional_weight > p_weight_kg THEN
        v_chargeable_weight := v_dimensional_weight;
    ELSE
        v_chargeable_weight := p_weight_kg;
    END IF;
    
    -- Find applicable rate
    SELECT base_rate
    INTO v_rate
    FROM shipping_rates
    WHERE tenant_id = p_tenant_id
    AND carrier_service_id = p_carrier_service_id
    AND origin_country = p_origin_country
    AND (origin_postal_code IS NULL OR origin_postal_code = p_origin_postal_code)
    AND destination_country = p_dest_country
    AND (destination_postal_code IS NULL OR destination_postal_code = p_dest_postal_code)
    AND v_chargeable_weight BETWEEN min_weight_kg AND max_weight_kg
    AND (effective_to IS NULL OR effective_to >= CURRENT_DATE)
    AND effective_from <= CURRENT_DATE
    ORDER BY 
        -- Prefer more specific postal code matches
        CASE 
            WHEN origin_postal_code IS NOT NULL AND destination_postal_code IS NOT NULL THEN 1
            WHEN origin_postal_code IS NOT NULL OR destination_postal_code IS NOT NULL THEN 2
            ELSE 3
        END,
        -- Then by effective date (most recent first)
        effective_from DESC
    LIMIT 1;
    
    RETURN v_rate;
END;
$$ LANGUAGE plpgsql;

-- Function to create shipment from sales order
CREATE OR REPLACE FUNCTION create_shipment_from_order(
    p_order_id INT,
    p_created_by INT
)
RETURNS INT AS $$
DECLARE
    v_shipment_id INT;
    v_shipment_number VARCHAR(50);
    v_tenant_id INT;
    v_order_number VARCHAR(50);
    v_client_id INT;
    v_origin_facility_id INT;
BEGIN
    -- Get order details
    SELECT so.tenant_id, so.order_number, so.client_id, so.facility_id
    INTO v_tenant_id, v_order_number, v_client_id, v_origin_facility_id
    FROM sales_orders so
    WHERE so.order_id = p_order_id;
    
    IF v_tenant_id IS NULL THEN
        RAISE EXCEPTION 'Sales order not found';
    END IF;
    
    -- Generate shipment number
    SELECT 'SHP-' || to_char(CURRENT_DATE, 'YYYYMM') || '-' || 
           lpad((COALESCE(MAX(SUBSTRING(shipment_number, 'SHP-\d{6}-(\d+)')::INT), 0) + 1)::TEXT, 5, '0')
    INTO v_shipment_number
    FROM shipments
    WHERE shipment_number ~ ('^SHP-' || to_char(CURRENT_DATE, 'YYYYMM') || '-\d+$')
    AND tenant_id = v_tenant_id;
    
    -- Create shipment
    INSERT INTO shipments (
        tenant_id, shipment_number, status,
        order_id, order_number, client_id,
        origin_facility_id, created_by, updated_by
    ) VALUES (
        v_tenant_id, v_shipment_number, 'PLANNED',
        p_order_id, v_order_number, v_client_id,
        v_origin_facility_id, p_created_by, p_created_by
    )
    RETURNING shipment_id INTO v_shipment_id;
    
    RETURN v_shipment_id;
END;
$$ LANGUAGE plpgsql;