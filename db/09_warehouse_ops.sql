-- ===================================================
-- MODULE: WAREHOUSE OPERATIONS
-- ===================================================

-- Task status
CREATE TYPE task_status AS ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'ON_HOLD', 'PARTIALLY_COMPLETED');

-- Task types
CREATE TYPE task_type AS ENUM('PUT_AWAY', 'PICKING', 'REPLENISHMENT', 'COUNTING', 'QUALITY_CHECK', 'PACKING', 'LOADING');

-- Work order status
CREATE TYPE work_order_status AS ENUM('DRAFT', 'PLANNED', 'RELEASED', 'IN_PROGRESS', 'COMPLETED', 'CLOSED', 'CANCELLED');

-- Work order types
CREATE TYPE work_order_type AS ENUM('MANUFACTURE', 'ASSEMBLY', 'REPAIR', 'KITTING', 'DECONSTRUCTION');

-- Wave status
CREATE TYPE wave_status AS ENUM('PENDING', 'RELEASED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

-- Put-away tasks
CREATE TABLE putaway_tasks (
    task_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    task_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    reference_type VARCHAR(20) NOT NULL, -- PURCHASE_RECEIPT, RETURN, TRANSFER_IN, INVENTORY_ADJUSTMENT
    reference_id INT NOT NULL,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_to_put_away NUMERIC(12,4) NOT NULL,
    quantity_put_away NUMERIC(12,4) DEFAULT 0.00,
    quantity_remaining NUMERIC(12,4) NOT NULL, -- Computed as (to_put_away - put_away)
    
    -- Location Information
    source_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    suggested_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    actual_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Status Information
    task_status task_status DEFAULT 'PENDING',
    priority INT DEFAULT 5, -- 1=highest, 10=lowest
    
    -- Timing
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    
    -- Status and Metadata
    notes TEXT,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    assigned_to INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_putaway_tasks_updated_at
BEFORE UPDATE ON putaway_tasks
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Picking tasks
CREATE TABLE picking_tasks (
    task_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    task_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    reference_type VARCHAR(20) NOT NULL, -- SALES_ORDER, TRANSFER_OUT, RETURN, PRODUCTION_ORDER
    reference_id INT NOT NULL,
    reference_line_id INT,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    lot_id INT REFERENCES inventory_lots(lot_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_to_pick NUMERIC(12,4) NOT NULL,
    quantity_picked NUMERIC(12,4) DEFAULT 0.00,
    quantity_remaining NUMERIC(12,4) NOT NULL, -- Computed as (to_pick - picked)
    
    -- Location Information
    source_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    destination_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Status Information
    task_status task_status DEFAULT 'PENDING',
    priority INT DEFAULT 5, -- 1=highest, 10=lowest
    
    -- Timing
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    
    -- Status and Metadata
    notes TEXT,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    assigned_to INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_picking_tasks_updated_at
BEFORE UPDATE ON picking_tasks
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Replenishment tasks
CREATE TABLE replenishment_tasks (
    task_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    task_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Task Information
    task_type VARCHAR(50) NOT NULL, -- PICK_LOCATION_REPLENISH, BULK_REPLENISH, CYCLE_COUNT_REPLENISH
    priority INT DEFAULT 5, -- 1=highest, 10=lowest
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_to_replenish NUMERIC(12,4) NOT NULL,
    quantity_replenished NUMERIC(12,4) DEFAULT 0.00,
    quantity_remaining NUMERIC(12,4) NOT NULL, -- Computed as (to_replenish - replenished)
    
    -- Location Information
    from_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    to_location_id INT NOT NULL REFERENCES storage_locations(location_id) ON DELETE CASCADE,
    
    -- Status Information
    task_status task_status DEFAULT 'PENDING',
    
    -- Trigger Information
    trigger_type VARCHAR(50) NOT NULL, -- MIN_QTY, WAVE_REPLENISH, MANUAL
    trigger_reference_id INT, -- Reference to the trigger (e.g., wave ID, user ID)
    
    -- Timing
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    
    -- Status and Metadata
    notes TEXT,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    assigned_to INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_replenishment_tasks_updated_at
BEFORE UPDATE ON replenishment_tasks
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Work orders (for manufacturing/assembly)
CREATE TABLE work_orders (
    work_order_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    work_order_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Order Information
    work_order_type work_order_type NOT NULL,
    priority INT DEFAULT 5, -- 1=highest, 10=lowest
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_ordered NUMERIC(12,4) NOT NULL,
    quantity_completed NUMERIC(12,4) DEFAULT 0.00,
    quantity_remaining NUMERIC(12,4) NOT NULL, -- Computed as (ordered - completed)
    
    -- Location Information
    production_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Scheduling
    start_date DATE,
    due_date DATE,
    actual_start_date TIMESTAMPTZ,
    actual_end_date TIMESTAMPTZ,
    
    -- Status Information
    work_order_status work_order_status DEFAULT 'DRAFT',
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_work_orders_updated_at
BEFORE UPDATE ON work_orders
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Work order components
CREATE TABLE work_order_components (
    component_id SERIAL PRIMARY KEY,
    work_order_id INT NOT NULL REFERENCES work_orders(work_order_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Component Information
    component_type VARCHAR(20) NOT NULL, -- RAW_MATERIAL, SUB_ASSEMBLY, TOOL
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_required NUMERIC(12,4) NOT NULL,
    quantity_issued NUMERIC(12,4) DEFAULT 0.00,
    quantity_used NUMERIC(12,4) DEFAULT 0.00,
    quantity_remaining NUMERIC(12,4) NOT NULL, -- Computed as (required - used)
    
    -- Location Information
    issue_from_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Status Information
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, ISSUED, PARTIALLY_ISSUED, COMPLETED
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_work_order_components_updated_at
BEFORE UPDATE ON work_order_components
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Picking waves
CREATE TABLE picking_waves (
    wave_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    wave_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Wave Information
    wave_name VARCHAR(100),
    priority INT DEFAULT 5, -- 1=highest, 10=lowest
    
    -- Status Information
    wave_status wave_status DEFAULT 'PENDING',
    
    -- Scheduling
    planned_date DATE,
    release_date TIMESTAMPTZ,
    completion_date TIMESTAMPTZ,
    
    -- Statistics
    total_orders INT DEFAULT 0,
    total_items INT DEFAULT 0,
    total_quantity NUMERIC(12,4) DEFAULT 0.00,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_picking_waves_updated_at
BEFORE UPDATE ON picking_waves
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Picking wave assignments
CREATE TABLE picking_wave_assignments (
    assignment_id SERIAL PRIMARY KEY,
    wave_id INT NOT NULL REFERENCES picking_waves(wave_id) ON DELETE CASCADE,
    order_id INT NOT NULL REFERENCES sales_orders(order_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Assignment Information
    priority INT DEFAULT 5, -- 1=highest, 10=lowest
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(wave_id, order_id)
);

CREATE TRIGGER set_picking_wave_assignments_updated_at
BEFORE UPDATE ON picking_wave_assignments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Pick paths
CREATE TABLE pick_paths (
    path_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    path_name VARCHAR(100) NOT NULL,
    zone_id INT REFERENCES storage_zones(zone_id) ON DELETE SET NULL,
    
    -- Path Information
    sequence_length INT,
    estimated_time_minutes INT,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_pick_paths_updated_at
BEFORE UPDATE ON pick_paths
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Pick path segments
CREATE TABLE pick_path_segments (
    segment_id SERIAL PRIMARY KEY,
    path_id INT NOT NULL REFERENCES pick_paths(path_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Segment Information
    sequence_number INT NOT NULL,
    from_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    to_location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    distance_meters NUMERIC(8,2),
    estimated_time_seconds INT,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(path_id, sequence_number)
);

CREATE TRIGGER set_pick_path_segments_updated_at
BEFORE UPDATE ON pick_path_segments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Task assignments
CREATE TABLE task_assignments (
    assignment_id SERIAL PRIMARY KEY,
    task_id INT NOT NULL,
    task_type task_type NOT NULL, -- PUT_AWAY, PICKING, REPLENISHMENT, etc.
    assigned_to INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Assignment Information
    assigned_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    status task_status DEFAULT 'PENDING',
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_task_assignments_updated_at
BEFORE UPDATE ON task_assignments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Indexes for better performance
CREATE INDEX idx_putaway_tasks_tenant ON putaway_tasks(tenant_id);
CREATE INDEX idx_putaway_tasks_reference ON putaway_tasks(reference_type, reference_id);
CREATE INDEX idx_putaway_tasks_status ON putaway_tasks(task_status);
CREATE INDEX idx_putaway_tasks_priority ON putaway_tasks(priority);
CREATE INDEX idx_picking_tasks_tenant ON picking_tasks(tenant_id);
CREATE INDEX idx_picking_tasks_reference ON picking_tasks(reference_type, reference_id);
CREATE INDEX idx_picking_tasks_status ON picking_tasks(task_status);
CREATE INDEX idx_picking_tasks_priority ON picking_tasks(priority);
CREATE INDEX idx_replenishment_tasks_tenant ON replenishment_tasks(tenant_id);
CREATE INDEX idx_replenishment_tasks_status ON replenishment_tasks(task_status);
CREATE INDEX idx_replenishment_tasks_priority ON replenishment_tasks(priority);
CREATE INDEX idx_work_orders_tenant ON work_orders(tenant_id);
CREATE INDEX idx_work_orders_status ON work_orders(work_order_status);
CREATE INDEX idx_work_orders_type ON work_orders(work_order_type);
CREATE INDEX idx_work_order_components_work_order ON work_order_components(work_order_id);
CREATE INDEX idx_work_order_components_product ON work_order_components(product_id);
CREATE INDEX idx_picking_waves_tenant ON picking_waves(tenant_id);
CREATE INDEX idx_picking_waves_status ON picking_waves(wave_status);
CREATE INDEX idx_picking_wave_assignments_wave ON picking_wave_assignments(wave_id);
CREATE INDEX idx_picking_wave_assignments_order ON picking_wave_assignments(order_id);
CREATE INDEX idx_pick_paths_tenant ON pick_paths(tenant_id);
CREATE INDEX idx_pick_path_segments_path ON pick_path_segments(path_id);
CREATE INDEX idx_task_assignments_task ON task_assignments(task_id, task_type);
CREATE INDEX idx_task_assignments_user ON task_assignments(assigned_to);