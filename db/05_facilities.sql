-- ===================================================
-- MODULE: FACILITIES
-- ===================================================

-- Facility types
CREATE TYPE facility_type AS ENUM('WAREHOUSE', 'DISTRIBUTION_CENTER', 'MANUFACTURING_PLANT', 'RETAIL_STORE', 'OFFICE');

-- Storage zone types
CREATE TYPE zone_type AS ENUM('BULK', 'RACK', 'COLD_STORAGE', 'HAZMAT', 'PICKING', 'STAGING', 'RECEIVING', 'SHIPPING', 'QA', 'OVERFLOW');

-- Storage location types
CREATE TYPE location_type AS ENUM('FLOOR', 'RACK', 'SHELF', 'BIN', 'PALLET_RACK', 'DRAWER', 'CAGE', 'DOCK');

-- Temperature zones
CREATE TYPE temperature_zone AS ENUM('AMBIENT', 'REFRIGERATED', 'FROZEN', 'CONTROLLED');

-- Warehouse facilities
CREATE TABLE warehouse_facilities (
    facility_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_name VARCHAR(100) NOT NULL,
    facility_code VARCHAR(20) UNIQUE,
    facility_type facility_type DEFAULT 'WAREHOUSE',
    
    -- Location Information
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country_id INT REFERENCES countries(country_id) ON DELETE SET NULL,
    postal_code VARCHAR(20),
    
    -- Contact Information
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    
    -- Operational Details
    is_active BOOLEAN DEFAULT TRUE,
    operational_hours VARCHAR(100),
    total_storage_capacity NUMERIC(12,2), -- in square meters
    total_volume_capacity NUMERIC(12,2), -- in cubic meters
    max_weight_capacity NUMERIC(12,2), -- in kg
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_warehouse_facilities_updated_at
BEFORE UPDATE ON warehouse_facilities
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Storage zones within a facility
CREATE TABLE storage_zones (
    zone_id SERIAL PRIMARY KEY,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    zone_name VARCHAR(50) NOT NULL,
    zone_code VARCHAR(20) NOT NULL,
    zone_type zone_type,
    
    -- Zone Properties
    temperature_zone temperature_zone,
    max_weight NUMERIC(12,2), -- maximum weight capacity in kg
    max_volume NUMERIC(12,2), -- maximum volume in cubic meters
    length_m NUMERIC(10,2), -- length in meters
    width_m NUMERIC(10,2), -- width in meters
    height_m NUMERIC(10,2), -- height in meters
    
    -- Status Information
    is_active BOOLEAN DEFAULT TRUE,
    is_temperature_controlled BOOLEAN DEFAULT FALSE,
    temperature_min NUMERIC(5,2), -- minimum temperature in Celsius
    temperature_max NUMERIC(5,2), -- maximum temperature in Celsius
    notes TEXT,
    
    -- Status and Metadata
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(facility_id, zone_code)
);

CREATE TRIGGER set_storage_zones_updated_at
BEFORE UPDATE ON storage_zones
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Storage locations (bins, racks, shelves)
CREATE TABLE storage_locations (
    location_id SERIAL PRIMARY KEY,
    zone_id INT NOT NULL REFERENCES storage_zones(zone_id) ON DELETE CASCADE,
    location_barcode VARCHAR(50) UNIQUE,
    location_name VARCHAR(50) NOT NULL,
    
    -- Location Properties
    location_type location_type NOT NULL,
    aisle VARCHAR(20),
    bay VARCHAR(20),
    level VARCHAR(20),
    position VARCHAR(20),
    max_weight NUMERIC(10,2), -- maximum weight capacity in kg
    max_volume NUMERIC(10,2), -- maximum volume in cubic meters
    length_m NUMERIC(10,2), -- length in meters
    width_m NUMERIC(10,2), -- width in meters
    height_m NUMERIC(10,2), -- height in meters
    
    -- Status Information
    is_active BOOLEAN DEFAULT TRUE,
    is_occupied BOOLEAN DEFAULT FALSE,
    last_inventory_date TIMESTAMPTZ,
    next_count_date TIMESTAMPTZ,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(zone_id, location_name)
);

CREATE TRIGGER set_storage_locations_updated_at
BEFORE UPDATE ON storage_locations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Equipment types
CREATE TYPE equipment_type AS ENUM('FORKLIFT', 'PALLET_JACK', 'HAND_TRUCK', 'CONVEYOR', 'SCANNER', 'PRINTER', 'COMPUTER', 'OTHER');

-- Equipment status
CREATE TYPE equipment_status AS ENUM('AVAILABLE', 'IN_USE', 'MAINTENANCE', 'OUT_OF_SERVICE', 'DECOMMISSIONED');

-- Warehouse equipment
CREATE TABLE warehouse_equipment (
    equipment_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    equipment_name VARCHAR(100) NOT NULL,
    equipment_type equipment_type NOT NULL,
    equipment_code VARCHAR(50) UNIQUE,
    
    -- Equipment Details
    model_number VARCHAR(50),
    serial_number VARCHAR(50),
    purchase_date DATE,
    purchase_price NUMERIC(12,2),
    warranty_expiry_date DATE,
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    
    -- Status Information
    status equipment_status DEFAULT 'AVAILABLE',
    current_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_warehouse_equipment_updated_at
BEFORE UPDATE ON warehouse_equipment
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Equipment maintenance records
CREATE TABLE equipment_maintenance (
    maintenance_id SERIAL PRIMARY KEY,
    equipment_id INT NOT NULL REFERENCES warehouse_equipment(equipment_id) ON DELETE CASCADE,
    maintenance_type VARCHAR(50), -- PREVENTIVE, CORRECTIVE, EMERGENCY
    maintenance_date DATE NOT NULL,
    next_maintenance_date DATE,
    technician_name VARCHAR(100),
    maintenance_cost NUMERIC(12,2),
    hours_worked NUMERIC(8,2),
    parts_replaced TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_equipment_maintenance_updated_at
BEFORE UPDATE ON equipment_maintenance
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Facility access levels
CREATE TYPE access_level AS ENUM('FULL', 'LIMITED', 'RESTRICTED');

-- Facility access control
CREATE TABLE facility_access_control (
    access_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    access_level access_level DEFAULT 'RESTRICTED',
    
    -- Access Details
    valid_from TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMPTZ,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(user_id, facility_id)
);

CREATE TRIGGER set_facility_access_control_updated_at
BEFORE UPDATE ON facility_access_control
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Dock types
CREATE TYPE dock_type AS ENUM('RECEIVING', 'SHIPPING', 'BOTH');

-- Loading docks
CREATE TABLE loading_docks (
    dock_id SERIAL PRIMARY KEY,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    dock_name VARCHAR(50) NOT NULL,
    dock_number VARCHAR(20),
    dock_type dock_type DEFAULT 'BOTH',
    is_active BOOLEAN DEFAULT TRUE,
    max_weight_capacity NUMERIC(12,2), -- in kg
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_loading_docks_updated_at
BEFORE UPDATE ON loading_docks
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Indexes for better performance
CREATE INDEX idx_warehouse_facilities_tenant ON warehouse_facilities(tenant_id);
CREATE INDEX idx_storage_zones_facility ON storage_zones(facility_id);
CREATE INDEX idx_storage_locations_zone ON storage_locations(zone_id);
CREATE INDEX idx_warehouse_equipment_tenant ON warehouse_equipment(tenant_id);
CREATE INDEX idx_warehouse_equipment_facility ON warehouse_equipment(facility_id);
CREATE INDEX idx_equipment_maintenance_equipment ON equipment_maintenance(equipment_id);
CREATE INDEX idx_facility_access_user ON facility_access_control(user_id);
CREATE INDEX idx_facility_access_facility ON facility_access_control(facility_id);
CREATE INDEX idx_loading_docks_facility ON loading_docks(facility_id);