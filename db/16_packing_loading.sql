-- ===================================================
-- MODULE: PACKING & LOADING
-- ===================================================
-- Complete packing and loading dock management

-- Packing Status Types
CREATE TYPE packing_status AS ENUM (
    'PENDING',
    'IN_PROGRESS',
    'PACKED',
    'VERIFIED',
    'CANCELLED'
);

-- Load Status Types
CREATE TYPE load_status AS ENUM (
    'PLANNED',
    'IN_PROGRESS',
    'LOADED',
    'IN_TRANSIT',
    'DELIVERED',
    'CANCELLED'
);

-- Packing Stations
CREATE TABLE packing_stations (
    station_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    
    -- Station Details
    station_code VARCHAR(20) NOT NULL,
    station_name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- Location
    location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Station Attributes
    is_active BOOLEAN DEFAULT TRUE,
    is_automated BOOLEAN DEFAULT FALSE,
    
    -- Dimensions (for automated systems)
    max_weight_kg DECIMAL(10,3),
    max_length_cm DECIMAL(8,2),
    max_width_cm DECIMAL(8,2),
    max_height_cm DECIMAL(8,2),
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, facility_id, station_code)
);

-- Packing Slips
CREATE TABLE packing_slips (
    packing_slip_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Slip Details
    slip_number VARCHAR(50) NOT NULL,
    status packing_status NOT NULL DEFAULT 'PENDING',
    
    -- References
    shipment_id INT REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    order_id INT, -- References sales_orders(order_id)
    
    -- Packing Details
    packed_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    packed_at TIMESTAMPTZ,
    verified_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    verified_at TIMESTAMPTZ,
    
    -- Package Information
    package_count INT DEFAULT 0,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, slip_number),
    CHECK (shipment_id IS NOT NULL OR order_id IS NOT NULL)
);

-- Packing Slip Items
CREATE TABLE packing_slip_items (
    slip_item_id SERIAL PRIMARY KEY,
    packing_slip_id INT NOT NULL REFERENCES packing_slips(packing_slip_id) ON DELETE CASCADE,
    
    -- Item Details
    item_sequence INT NOT NULL,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    quantity_ordered NUMERIC(12,4) NOT NULL,
    quantity_packed NUMERIC(12,4) DEFAULT 0,
    
    -- Package Assignment
    package_id INT REFERENCES packages(package_id) ON DELETE SET NULL,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PACKED, PARTIALLY_PACKED, CANCELLED
    
    -- Audit Fields
    packed_at TIMESTAMPTZ,
    packed_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(packing_slip_id, item_sequence)
);

-- Loads
CREATE TABLE loads (
    load_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    
    -- Load Details
    load_number VARCHAR(50) NOT NULL,
    status load_status NOT NULL DEFAULT 'PLANNED',
    
    -- Transportation
    carrier_id INT REFERENCES carriers(carrier_id) ON DELETE SET NULL,
    vehicle_number VARCHAR(50),
    driver_name VARCHAR(100),
    driver_contact VARCHAR(50),
    
    -- Timing
    planned_departure TIMESTAMPTZ,
    actual_departure TIMESTAMPTZ,
    planned_arrival TIMESTAMPTZ,
    actual_arrival TIMESTAMPTZ,
    
    -- Dimensions
    total_weight_kg DECIMAL(10,3),
    total_volume_cbm DECIMAL(10,3),
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, load_number)
);

-- Load Items (Shipments)
CREATE TABLE load_shipments (
    load_shipment_id SERIAL PRIMARY KEY,
    load_id INT NOT NULL REFERENCES loads(load_id) ON DELETE CASCADE,
    shipment_id INT NOT NULL REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    
    -- Loading Details
    loading_sequence INT NOT NULL,
    loaded_at TIMESTAMPTZ,
    loaded_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Unloading Details
    unloaded_at TIMESTAMPTZ,
    unloaded_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, LOADED, UNLOADED, CANCELLED
    
    -- Constraints
    UNIQUE(load_id, shipment_id),
    UNIQUE(load_id, loading_sequence)
);

-- Loading Docks
CREATE TABLE loading_docks (
    dock_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    
    -- Dock Details
    dock_code VARCHAR(20) NOT NULL,
    dock_name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- Dock Attributes
    is_active BOOLEAN DEFAULT TRUE,
    is_outbound BOOLEAN DEFAULT TRUE,
    is_inbound BOOLEAN DEFAULT TRUE,
    
    -- Dimensions
    max_vehicle_length_meters DECIMAL(6,2),
    max_vehicle_weight_kg DECIMAL(10,3),
    
    -- Current Status
    current_load_id INT REFERENCES loads(load_id) ON DELETE SET NULL,
    current_status VARCHAR(20), -- AVAILABLE, OCCUPIED, MAINTENANCE
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, facility_id, dock_code)
);

-- Dock Appointments
CREATE TABLE dock_appointments (
    appointment_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    dock_id INT NOT NULL REFERENCES loading_docks(dock_id) ON DELETE CASCADE,
    
    -- Appointment Details
    appointment_number VARCHAR(50) NOT NULL,
    purpose VARCHAR(100) NOT NULL, -- LOADING, UNLOADING, MAINTENANCE, etc.
    
    -- Timing
    scheduled_start TIMESTAMPTZ NOT NULL,
    scheduled_end TIMESTAMPTZ NOT NULL,
    actual_start TIMESTAMPTZ,
    actual_end TIMESTAMPTZ,
    
    -- References
    load_id INT REFERENCES loads(load_id) ON DELETE CASCADE,
    shipment_id INT REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    
    -- Additional Info
    notes TEXT,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    CHECK (scheduled_end > scheduled_start),
    CHECK (actual_end IS NULL OR actual_end > actual_start),
    UNIQUE(tenant_id, appointment_number)
);

-- Packing Materials
CREATE TABLE packing_materials (
    material_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Material Details
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- Material Type
    material_type VARCHAR(50) NOT NULL, -- BOX, ENVELOPE, PALLET, STRETCH_WRAP, etc.
    
    -- Dimensions
    length_cm DECIMAL(8,2),
    width_cm DECIMAL(8,2),
    height_cm DECIMAL(8,2),
    weight_kg DECIMAL(8,3),
    
    -- Capacity
    max_weight_kg DECIMAL(10,3),
    
    -- Cost
    unit_cost DECIMAL(12,4),
    currency VARCHAR(3) DEFAULT 'USD',
    
    -- Inventory
    is_reusable BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, material_code)
);

-- Package Materials (Junction Table)
CREATE TABLE package_materials (
    package_id INT NOT NULL REFERENCES packages(package_id) ON DELETE CASCADE,
    material_id INT NOT NULL REFERENCES packing_materials(material_id) ON DELETE CASCADE,
    
    -- Quantity
    quantity_used NUMERIC(12,4) NOT NULL DEFAULT 1,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    PRIMARY KEY (package_id, material_id)
);

-- Function to create packing slip from shipment
CREATE OR REPLACE FUNCTION create_packing_slip(
    p_tenant_id INT,
    p_shipment_id INT,
    p_created_by INT
)
RETURNS INT AS $$
DECLARE
    v_slip_id INT;
    v_slip_number VARCHAR(50);
    v_order_id INT;
    v_order_number VARCHAR(50);
    v_facility_id INT;
    v_client_id INT;
BEGIN
    -- Get shipment details
    SELECT 
        s.order_id, 
        so.order_number,
        s.origin_facility_id,
        s.client_id
    INTO 
        v_order_id,
        v_order_number,
        v_facility_id,
        v_client_id
    FROM shipments s
    LEFT JOIN sales_orders so ON so.order_id = s.order_id
    WHERE s.shipment_id = p_shipment_id
    AND s.tenant_id = p_tenant_id;
    
    IF v_facility_id IS NULL THEN
        RAISE EXCEPTION 'Shipment not found or not eligible for packing';
    END IF;
    
    -- Generate packing slip number
    SELECT 'PS-' || to_char(CURRENT_DATE, 'YYYYMM') || '-' || 
           lpad((COALESCE(MAX(SUBSTRING(slip_number, 'PS-\d{6}-(\d+)')::INT), 0) + 1)::TEXT, 5, '0')
    INTO v_slip_number
    FROM packing_slips
    WHERE tenant_id = p_tenant_id
    AND slip_number ~ ('^PS-' || to_char(CURRENT_DATE, 'YYYYMM') || '-\d+$');
    
    -- Create packing slip
    INSERT INTO packing_slips (
        tenant_id,
        slip_number,
        status,
        shipment_id,
        order_id,
        created_by,
        updated_by
    ) VALUES (
        p_tenant_id,
        v_slip_number,
        'PENDING',
        p_shipment_id,
        v_order_id,
        p_created_by,
        p_created_by
    )
    RETURNING packing_slip_id INTO v_slip_id;
    
    -- Add items to packing slip
    INSERT INTO packing_slip_items (
        packing_slip_id,
        item_sequence,
        product_id,
        variant_id,
        quantity_ordered,
        status
    )
    SELECT 
        v_slip_id,
        ROW_NUMBER() OVER (ORDER BY soi.order_item_id) as item_sequence,
        soi.product_id,
        soi.variant_id,
        soi.quantity_ordered,
        'PENDING'
    FROM sales_order_items soi
    WHERE soi.order_id = v_order_id
    AND soi.tenant_id = p_tenant_id
    ORDER BY soi.order_item_id;
    
    -- Update package count
    UPDATE packing_slips
    SET package_count = (
        SELECT COUNT(DISTINCT p.package_id)
        FROM packages p
        WHERE p.shipment_id = p_shipment_id
        AND p.tenant_id = p_tenant_id
    )
    WHERE packing_slip_id = v_slip_id;
    
    RETURN v_slip_id;
END;
$$ LANGUAGE plpgsql;

-- Function to create load from shipments
CREATE OR REPLACE FUNCTION create_load(
    p_tenant_id INT,
    p_facility_id INT,
    p_shipment_ids INT[],
    p_carrier_id INT,
    p_planned_departure TIMESTAMPTZ,
    p_planned_arrival TIMESTAMPTZ,
    p_created_by INT
)
RETURNS INT AS $$
DECLARE
    v_load_id INT;
    v_load_number VARCHAR(50);
    v_sequence INT := 1;
    v_shipment_id INT;
    v_total_weight_kg DECIMAL(10,3) := 0;
    v_total_volume_cbm DECIMAL(10,3) := 0;
    v_vehicle_number VARCHAR(50);
    v_driver_name VARCHAR(100);
    v_driver_contact VARCHAR(50);
BEGIN
    -- Generate load number
    SELECT 'LD-' || to_char(CURRENT_DATE, 'YYYYMM') || '-' || 
           lpad((COALESCE(MAX(SUBSTRING(load_number, 'LD-\d{6}-(\d+)')::INT), 0) + 1)::TEXT, 5, '0')
    INTO v_load_number
    FROM loads
    WHERE tenant_id = p_tenant_id
    AND load_number ~ ('^LD-' || to_char(CURRENT_DATE, 'YYYYMM') || '-\d+$');
    
    -- Get carrier details if provided
    IF p_carrier_id IS NOT NULL THEN
        SELECT 
            carrier_name,
            COALESCE(carrier_code, 'UNKNOWN') as vehicle_num,
            COALESCE(carrier_name, 'UNKNOWN') as driver_name,
            COALESCE(carrier_code, '') as driver_contact
        INTO 
            v_driver_name,
            v_vehicle_number,
            v_driver_name,
            v_driver_contact
        FROM carriers
        WHERE carrier_id = p_carrier_id
        AND tenant_id = p_tenant_id;
    END IF;
    
    -- Calculate total weight and volume
    SELECT 
        COALESCE(SUM(s.total_weight_kg), 0),
        COALESCE(SUM(s.total_volume_cbm), 0)
    INTO 
        v_total_weight_kg,
        v_total_volume_cbm
    FROM shipments s
    WHERE s.shipment_id = ANY(p_shipment_ids)
    AND s.tenant_id = p_tenant_id
    AND s.status IN ('PACKED', 'AWAITING_SHIPMENT');
    
    -- Create load
    INSERT INTO loads (
        tenant_id,
        facility_id,
        load_number,
        status,
        carrier_id,
        vehicle_number,
        driver_name,
        driver_contact,
        planned_departure,
        planned_arrival,
        total_weight_kg,
        total_volume_cbm,
        created_by,
        updated_by
    ) VALUES (
        p_tenant_id,
        p_facility_id,
        v_load_number,
        'PLANNED',
        p_carrier_id,
        v_vehicle_number,
        v_driver_name,
        v_driver_contact,
        p_planned_departure,
        p_planned_arrival,
        v_total_weight_kg,
        v_total_volume_cbm,
        p_created_by,
        p_created_by
    )
    RETURNING load_id INTO v_load_id;
    
    -- Add shipments to load
    FOREACH v_shipment_id IN ARRAY p_shipment_ids
    LOOP
        INSERT INTO load_shipments (
            load_id,
            shipment_id,
            loading_sequence,
            status
        ) VALUES (
            v_load_id,
            v_shipment_id,
            v_sequence,
            'PENDING'
        );
        
        -- Update shipment status
        UPDATE shipments
        SET status = 'AWAITING_LOADING',
            updated_at = CURRENT_TIMESTAMP,
            updated_by = p_created_by
        WHERE shipment_id = v_shipment_id
        AND tenant_id = p_tenant_id;
        
        v_sequence := v_sequence + 1;
    END LOOP;
    
    RETURN v_load_id;
END;
$$ LANGUAGE plpgsql;

-- Function to assign dock to load
CREATE OR REPLACE FUNCTION assign_dock_to_load(
    p_tenant_id INT,
    p_load_id INT,
    p_dock_id INT,
    p_scheduled_start TIMESTAMPTZ,
    p_scheduled_end TIMESTAMPTZ,
    p_created_by INT
)
RETURNS INT AS $$
DECLARE
    v_appointment_id INT;
    v_facility_id INT;
    v_dock_available BOOLEAN := TRUE;
    v_appointment_number VARCHAR(50);
BEGIN
    -- Check if dock is available
    SELECT EXISTS (
        SELECT 1 
        FROM dock_appointments da
        JOIN loads l ON l.load_id = da.load_id
        WHERE da.dock_id = p_dock_id
        AND da.tenant_id = p_tenant_id
        AND (
            (p_scheduled_start BETWEEN da.scheduled_start AND da.scheduled_end)
            OR (p_scheduled_end BETWEEN da.scheduled_start AND da.scheduled_end)
            OR (da.scheduled_start BETWEEN p_scheduled_start AND p_scheduled_end)
        )
        AND da.status NOT IN ('COMPLETED', 'CANCELLED')
        AND l.status NOT IN ('DELIVERED', 'CANCELLED')
    ) INTO v_dock_available;
    
    IF NOT v_dock_available THEN
        RAISE EXCEPTION 'Dock is not available during the requested time slot';
    END IF;
    
    -- Get facility ID from load
    SELECT facility_id INTO v_facility_id
    FROM loads
    WHERE load_id = p_load_id
    AND tenant_id = p_tenant_id;
    
    IF v_facility_id IS NULL THEN
        RAISE EXCEPTION 'Load not found';
    END IF;
    
    -- Generate appointment number
    SELECT 'APT-' || to_char(CURRENT_DATE, 'YYYYMM') || '-' || 
           lpad((COALESCE(MAX(SUBSTRING(appointment_number, 'APT-\d{6}-(\d+)')::INT), 0) + 1)::TEXT, 5, '0')
    INTO v_appointment_number
    FROM dock_appointments
    WHERE tenant_id = p_tenant_id
    AND appointment_number ~ ('^APT-' || to_char(CURRENT_DATE, 'YYYYMM') || '-\d+$');
    
    -- Create dock appointment
    INSERT INTO dock_appointments (
        tenant_id,
        dock_id,
        appointment_number,
        purpose,
        scheduled_start,
        scheduled_end,
        load_id,
        status,
        created_by,
        updated_by
    ) VALUES (
        p_tenant_id,
        p_dock_id,
        v_appointment_number,
        'LOADING',
        p_scheduled_start,
        p_scheduled_end,
        p_load_id,
        'SCHEDULED',
        p_created_by,
        p_created_by
    )
    RETURNING appointment_id INTO v_appointment_id;
    
    -- Update dock status
    UPDATE loading_docks
    SET current_status = 'RESERVED',
        current_load_id = p_load_id,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = p_created_by
    WHERE dock_id = p_dock_id
    AND tenant_id = p_tenant_id;
    
    -- Update load status
    UPDATE loads
    SET status = 'SCHEDULED',
        updated_at = CURRENT_TIMESTAMP,
        updated_by = p_created_by
    WHERE load_id = p_load_id
    AND tenant_id = p_tenant_id;
    
    RETURN v_appointment_id;
END;
$$ LANGUAGE plpgsql;

-- Audit Triggers
CREATE TRIGGER set_packing_stations_updated_at
BEFORE UPDATE ON packing_stations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_packing_slips_updated_at
BEFORE UPDATE ON packing_slips
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_loads_updated_at
BEFORE UPDATE ON loads
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_loading_docks_updated_at
BEFORE UPDATE ON loading_docks
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_dock_appointments_updated_at
BEFORE UPDATE ON dock_appointments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_packing_materials_updated_at
BEFORE UPDATE ON packing_materials
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();