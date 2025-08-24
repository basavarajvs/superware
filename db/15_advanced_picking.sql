-- ===================================================
-- MODULE: ADVANCED PICKING
-- ===================================================
-- Sophisticated picking optimization and path planning

-- Pick Strategy Types
CREATE TYPE pick_strategy AS ENUM (
    'SINGLE_ORDER',
    'BATCH',
    'WAVE',
    'ZONE',
    'CLUSTER',
    'PICK_AND_PASS'
);

-- Pick Path Types
CREATE TYPE pick_path_type AS ENUM (
    'SHORTEST_PATH',
    'SEQUENTIAL',
    'CUSTOM'
);

-- Pick Status Types
CREATE TYPE pick_status AS ENUM (
    'PENDING',
    'IN_PROGRESS',
    'PAUSED',
    'COMPLETED',
    'CANCELLED'
);

-- Pick Zones
CREATE TABLE pick_zones (
    zone_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    
    -- Zone Details
    zone_code VARCHAR(20) NOT NULL,
    zone_name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- Zone Attributes
    is_active BOOLEAN DEFAULT TRUE,
    is_overflow BOOLEAN DEFAULT FALSE,
    
    -- Zone Bounds
    min_aisle VARCHAR(10),
    max_aisle VARCHAR(10),
    min_level INT,
    max_level INT,
    
    -- Performance Metrics
    average_pick_time_seconds INT,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, facility_id, zone_code)
);

-- Pick Paths
CREATE TABLE pick_paths (
    path_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    
    -- Path Details
    path_name VARCHAR(100) NOT NULL,
    path_type pick_path_type NOT NULL,
    description TEXT,
    
    -- Path Definition
    start_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    end_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Performance Metrics
    average_completion_time_seconds INT,
    total_distance_meters DECIMAL(10,2),
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, facility_id, path_name)
);

-- Pick Path Segments
CREATE TABLE pick_path_segments (
    segment_id SERIAL PRIMARY KEY,
    path_id INT NOT NULL REFERENCES pick_paths(path_id) ON DELETE CASCADE,
    
    -- Segment Details
    segment_sequence INT NOT NULL,
    from_location_id INT NOT NULL REFERENCES storage_locations(location_id) ON DELETE CASCADE,
    to_location_id INT NOT NULL REFERENCES storage_locations(location_id) ON DELETE CASCADE,
    
    -- Segment Metrics
    distance_meters DECIMAL(10,2),
    estimated_travel_time_seconds INT,
    
    -- Constraints
    UNIQUE(path_id, segment_sequence)
);

-- Pick Routes
CREATE TABLE pick_routes (
    route_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    
    -- Route Details
    route_name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- Route Configuration
    strategy pick_strategy NOT NULL,
    max_items_per_picker INT,
    max_weight_per_pick DECIMAL(10,3),
    max_volume_per_pick DECIMAL(10,3),
    
    -- Performance Metrics
    average_picks_per_hour DECIMAL(10,2),
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, facility_id, route_name)
);

-- Pick Route Zones
CREATE TABLE pick_route_zones (
    route_id INT NOT NULL REFERENCES pick_routes(route_id) ON DELETE CASCADE,
    zone_id INT NOT NULL REFERENCES pick_zones(zone_id) ON DELETE CASCADE,
    zone_sequence INT NOT NULL,
    
    -- Constraints
    PRIMARY KEY (route_id, zone_id),
    UNIQUE(route_id, zone_sequence)
);

-- Pick Tasks
CREATE TABLE pick_tasks (
    pick_task_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    
    -- Task Details
    task_number VARCHAR(50) NOT NULL,
    status pick_status NOT NULL DEFAULT 'PENDING',
    priority INT DEFAULT 5, -- 1=Highest, 10=Lowest
    
    -- References
    pick_route_id INT REFERENCES pick_routes(route_id) ON DELETE SET NULL,
    pick_path_id INT REFERENCES pick_paths(path_id) ON DELETE SET NULL,
    assigned_to INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Timing
    planned_start_time TIMESTAMPTZ,
    actual_start_time TIMESTAMPTZ,
    planned_end_time TIMESTAMPTZ,
    actual_end_time TIMESTAMPTZ,
    
    -- Performance
    total_items INT DEFAULT 0,
    total_locations INT DEFAULT 0,
    total_distance_meters DECIMAL(10,2),
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, task_number)
);

-- Pick Task Items
CREATE TABLE pick_task_items (
    pick_task_item_id SERIAL PRIMARY KEY,
    pick_task_id INT NOT NULL REFERENCES pick_tasks(pick_task_id) ON DELETE CASCADE,
    
    -- Item Details
    item_sequence INT NOT NULL,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    quantity_required NUMERIC(12,4) NOT NULL,
    quantity_picked NUMERIC(12,4) DEFAULT 0,
    
    -- Location
    location_id INT NOT NULL REFERENCES storage_locations(location_id) ON DELETE CASCADE,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PICKED, PARTIALLY_PICKED, CANCELLED
    
    -- Audit Fields
    picked_at TIMESTAMPTZ,
    picked_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(pick_task_id, item_sequence)
);

-- Pick Lists
CREATE TABLE pick_lists (
    pick_list_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- List Details
    list_number VARCHAR(50) NOT NULL,
    status pick_status NOT NULL DEFAULT 'PENDING',
    
    -- References
    pick_task_id INT REFERENCES pick_tasks(pick_task_id) ON DELETE CASCADE,
    order_id INT, -- References sales_orders(order_id)
    shipment_id INT, -- References shipments(shipment_id)
    
    -- Timing
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    
    -- Constraints
    UNIQUE(tenant_id, list_number)
);

-- Pick List Items
CREATE TABLE pick_list_items (
    pick_list_item_id SERIAL PRIMARY KEY,
    pick_list_id INT NOT NULL REFERENCES pick_lists(pick_list_id) ON DELETE CASCADE,
    pick_task_item_id INT REFERENCES pick_task_items(pick_task_item_id) ON DELETE CASCADE,
    
    -- Item Details
    item_sequence INT NOT NULL,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE CASCADE,
    quantity_required NUMERIC(12,4) NOT NULL,
    quantity_picked NUMERIC(12,4) DEFAULT 0,
    
    -- Location
    location_id INT NOT NULL REFERENCES storage_locations(location_id) ON DELETE CASCADE,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PICKED, PARTIALLY_PICKED, CANCELLED
    
    -- Constraints
    UNIQUE(pick_list_id, item_sequence)
);

-- Putaway Zones
CREATE TABLE putaway_zones (
    zone_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    facility_id INT NOT NULL REFERENCES warehouse_facilities(facility_id) ON DELETE CASCADE,
    
    -- Zone Details
    zone_code VARCHAR(20) NOT NULL,
    zone_name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- Zone Attributes
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Zone Rules
    storage_type VARCHAR(50), -- BULK, RACK, FLOOR, etc.
    temperature_zone VARCHAR(50), -- AMBIENT, COLD, FROZEN, etc.
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, facility_id, zone_code)
);

-- Putaway Rules
CREATE TABLE putaway_rules (
    rule_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Rule Details
    rule_name VARCHAR(100) NOT NULL,
    description TEXT,
    priority INT NOT NULL DEFAULT 5, -- 1=Highest, 10=Lowest
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Rule Conditions (JSONB for flexible conditions)
    conditions JSONB NOT NULL,
    
    -- Rule Actions
    target_zone_id INT REFERENCES putaway_zones(zone_id) ON DELETE CASCADE,
    max_quantity_per_location NUMERIC(12,4),
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, rule_name)
);

-- Function to generate optimal pick path
CREATE OR REPLACE FUNCTION generate_optimal_pick_path(
    p_tenant_id INT,
    p_facility_id INT,
    p_location_ids INT[],
    p_start_location_id INT DEFAULT NULL
)
RETURNS TABLE (
    location_id INT,
    location_code VARCHAR(50),
    sequence_number INT,
    distance_from_previous DECIMAL(10,2)
) AS $$
BEGIN
    -- This is a simplified version. A real implementation would use a pathfinding algorithm
    -- like A* or Dijkstra's algorithm to find the optimal path through the warehouse
    
    RETURN QUERY
    WITH locations AS (
        SELECT 
            sl.location_id,
            sl.location_name as location_code,
            sl.aisle,
            sl.bay,
            sl.level,
            sl.position,
            sl.length_m,
            sl.width_m,
            sl.height_m
        FROM storage_locations sl
        WHERE sl.tenant_id = p_tenant_id
        AND sl.facility_id = p_facility_id
        AND sl.location_id = ANY(p_location_ids)
    )
    SELECT 
        l.location_id,
        l.location_code,
        ROW_NUMBER() OVER (ORDER BY 
            -- Simple heuristic: sort by aisle, bay, level, position
            l.aisle, 
            l.bay, 
            l.level, 
            l.position
        ) as sequence_number,
        -- Calculate distance from previous location (simplified)
        CASE 
            WHEN LAG(l.location_id) OVER (ORDER BY l.aisle, l.bay, l.level, l.position) IS NULL THEN 0
            ELSE 1 -- Simplified distance
        END as distance_from_previous
    FROM locations l
    ORDER BY sequence_number;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate putaway location
CREATE OR REPLACE FUNCTION calculate_putaway_location(
    p_tenant_id INT,
    p_facility_id INT,
    p_product_id INT,
    p_variant_id INT,
    p_quantity NUMERIC(12,4),
    p_putaway_rule_id INT DEFAULT NULL
)
RETURNS TABLE (
    location_id INT,
    location_name VARCHAR(50),
    zone_id INT,
    zone_name VARCHAR(100),
    putaway_quantity NUMERIC(12,4)
) AS $$
BEGIN
    -- This is a simplified version. A real implementation would consider:
    -- 1. Available space in locations
    -- 2. Storage requirements (temperature, hazardous, etc.)
    -- 3. Putaway rules and priorities
    -- 4. Current inventory levels
    
    RETURN QUERY
    WITH putaway_candidates AS (
        SELECT 
            sl.location_id,
            sl.location_name,
            pz.zone_id,
            pz.zone_name,
            COALESCE(
                (SELECT putaway_quantity 
                 FROM calculate_putaway_quantity(
                     p_tenant_id, 
                     p_facility_id, 
                     p_product_id, 
                     p_variant_id,
                     sl.location_id, 
                     p_quantity
                 )),
                p_quantity
            ) as putaway_quantity,
            -- Simple scoring based on current inventory (lower is better)
            COALESCE(ioh.quantity_on_hand, 0) as current_quantity,
            -- Random factor to distribute putaways
            RANDOM() as random_factor
        FROM storage_locations sl
        JOIN storage_zones pz ON pz.zone_id = sl.zone_id
        LEFT JOIN inventory_on_hand ioh ON 
            ioh.location_id = sl.location_id 
            AND ioh.product_id = p_product_id
            AND (p_variant_id IS NULL OR ioh.variant_id = p_variant_id)
        WHERE sl.tenant_id = p_tenant_id
        AND sl.facility_id = p_facility_id
        AND sl.is_active = TRUE
        AND (p_putaway_rule_id IS NULL OR pz.zone_id IN (
            SELECT target_zone_id 
            FROM putaway_rules 
            WHERE rule_id = p_putaway_rule_id
            AND is_active = TRUE
        ))
        ORDER BY 
            -- Prefer locations with some existing inventory but not full
            ABS(COALESCE(ioh.quantity_on_hand, 0) - (sl.max_weight * 0.5)),
            -- Then randomize to distribute putaways
            random_factor
        LIMIT 10 -- Limit number of candidates to consider
    )
    SELECT 
        pc.location_id,
        pc.location_name,
        pc.zone_id,
        pc.zone_name,
        LEAST(pc.putaway_quantity, p_quantity) as putaway_quantity
    FROM putaway_candidates pc
    WHERE pc.putaway_quantity > 0
    ORDER BY 
        -- Prefer locations with some existing inventory but not full
        pc.current_quantity,
        -- Then by random factor
        pc.random_factor
    LIMIT 1;
END;
$$ LANGUAGE plpgsql;

-- Helper function to calculate putaway quantity
CREATE OR REPLACE FUNCTION calculate_putaway_quantity(
    p_tenant_id INT,
    p_facility_id INT,
    p_product_id INT,
    p_variant_id INT,
    p_location_id INT,
    p_quantity_available NUMERIC(12,4)
)
RETURNS NUMERIC(12,4) AS $$
DECLARE
    v_max_capacity NUMERIC(12,4);
    v_current_quantity NUMERIC(12,4);
    v_available_space NUMERIC(12,4);
    v_putaway_quantity NUMERIC(12,4);
BEGIN
    -- Get location capacity
    SELECT 
        sl.max_weight,
        COALESCE(SUM(ioh.quantity_on_hand), 0)
    INTO 
        v_max_capacity,
        v_current_quantity
    FROM storage_locations sl
    LEFT JOIN inventory_on_hand ioh ON 
        ioh.location_id = sl.location_id 
        AND ioh.tenant_id = sl.tenant_id
        AND ioh.product_id = p_product_id
        AND (p_variant_id IS NULL OR ioh.variant_id = p_variant_id)
    WHERE sl.tenant_id = p_tenant_id
    AND sl.location_id = p_location_id
    GROUP BY sl.location_id, sl.max_weight;
    
    -- Calculate available space
    v_available_space := GREATEST(0, v_max_capacity - v_current_quantity);
    
    -- Determine putaway quantity
    v_putaway_quantity := LEAST(v_available_space, p_quantity_available);
    
    RETURN v_putaway_quantity;
END;
$$ LANGUAGE plpgsql;

-- Audit Triggers
CREATE TRIGGER set_pick_zones_updated_at
BEFORE UPDATE ON pick_zones
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_pick_paths_updated_at
BEFORE UPDATE ON pick_paths
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_pick_routes_updated_at
BEFORE UPDATE ON pick_routes
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_pick_tasks_updated_at
BEFORE UPDATE ON pick_tasks
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_putaway_zones_updated_at
BEFORE UPDATE ON putaway_zones
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_putaway_rules_updated_at
BEFORE UPDATE ON putaway_rules
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();