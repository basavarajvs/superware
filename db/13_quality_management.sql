-- ===================================================
-- MODULE: QUALITY MANAGEMENT
-- ===================================================
-- Comprehensive quality management system

-- Quality Status Types
CREATE TYPE quality_status AS ENUM (
    'DRAFT',
    'PENDING',
    'IN_PROGRESS',
    'PASSED',
    'FAILED',
    'QUARANTINED',
    'CONCESSION',
    'REJECTED',
    'CANCELLED'
);

-- Quality Inspection Types
CREATE TYPE inspection_type AS ENUM (
    'INCOMING',
    'IN_PROCESS',
    'FINAL',
    'FIRST_ARTICLE',
    'RANDOM',
    'AUDIT',
    'RETURN',
    'CUSTOMER_COMPLAINT'
);

-- Quality Inspection Methods
CREATE TYPE inspection_method AS ENUM (
    'VISUAL',
    'MEASUREMENT',
    'FUNCTIONAL_TEST',
    'DESTRUCTIVE_TEST',
    'NON_DESTRUCTIVE_TEST',
    'SAMPLE',
    'AUDIT'
);

-- Quality Standards
CREATE TABLE quality_standards (
    standard_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    standard_code VARCHAR(50) NOT NULL,
    standard_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, standard_code)
);

-- Quality Inspection Plans
CREATE TABLE quality_inspection_plans (
    plan_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    plan_name VARCHAR(100) NOT NULL,
    description TEXT,
    inspection_type inspection_type NOT NULL,
    applicable_to_product BOOLEAN DEFAULT TRUE,
    applicable_to_process BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, plan_name)
);

-- Quality Inspection Plan Items
CREATE TABLE quality_inspection_plan_items (
    plan_item_id SERIAL PRIMARY KEY,
    plan_id INT NOT NULL REFERENCES quality_inspection_plans(plan_id) ON DELETE CASCADE,
    item_sequence INT NOT NULL,
    check_type VARCHAR(50) NOT NULL, -- 'VISUAL', 'DIMENSIONAL', 'FUNCTIONAL', etc.
    description TEXT NOT NULL,
    specification TEXT,
    method inspection_method NOT NULL,
    sample_size INT,
    sample_size_unit VARCHAR(20),
    min_value NUMERIC(12,4),
    max_value NUMERIC(12,4),
    target_value NUMERIC(12,4),
    uom VARCHAR(20),
    tolerance_type VARCHAR(20), -- 'PERCENTAGE', 'FIXED', 'NONE'
    tolerance_value NUMERIC(12,4),
    is_critical BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(plan_id, item_sequence)
);

-- Quality Inspections
CREATE TABLE quality_inspections (
    inspection_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    inspection_number VARCHAR(50) NOT NULL,
    inspection_type inspection_type NOT NULL,
    status quality_status NOT NULL DEFAULT 'DRAFT',
    reference_type VARCHAR(50), -- 'PURCHASE_ORDER', 'WORK_ORDER', 'SALES_ORDER', 'INVENTORY', etc.
    reference_id INT,
    reference_document VARCHAR(100),
    
    -- Product/Item Information
    product_id INT REFERENCES products(product_id) ON DELETE SET NULL,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    product_name VARCHAR(255),
    product_sku VARCHAR(100),
    
    -- Inspection Details
    planned_date DATE,
    actual_date TIMESTAMPTZ,
    planned_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    inspected_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Quantity Information
    quantity_inspected NUMERIC(12,4) DEFAULT 0,
    quantity_passed NUMERIC(12,4) DEFAULT 0,
    quantity_failed NUMERIC(12,4) DEFAULT 0,
    quantity_quarantined NUMERIC(12,4) DEFAULT 0,
    
    -- Location Information
    facility_id INT REFERENCES warehouse_facilities(facility_id) ON DELETE SET NULL,
    location_id INT REFERENCES storage_locations(location_id) ON DELETE SET NULL,
    
    -- Results
    result VARCHAR(20), -- 'PASS', 'FAIL', 'CONCESSION', 'REJECT'
    result_details JSONB,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    CHECK (quantity_inspected >= (quantity_passed + quantity_failed + quantity_quarantined)),
    UNIQUE(tenant_id, inspection_number)
);

-- Quality Inspection Items
CREATE TABLE quality_inspection_items (
    inspection_item_id SERIAL PRIMARY KEY,
    inspection_id INT NOT NULL REFERENCES quality_inspections(inspection_id) ON DELETE CASCADE,
    plan_item_id INT REFERENCES quality_inspection_plan_items(plan_item_id) ON DELETE SET NULL,
    
    -- Item Details
    item_sequence INT NOT NULL,
    check_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    specification TEXT,
    method inspection_method NOT NULL,
    
    -- Measurements/Results
    reading_1 NUMERIC(12,4),
    reading_2 NUMERIC(12,4),
    reading_3 NUMERIC(12,4),
    reading_4 NUMERIC(12,4),
    reading_5 NUMERIC(12,4),
    average_reading NUMERIC(12,4) GENERATED ALWAYS AS (
        (COALESCE(reading_1, 0) + COALESCE(reading_2, 0) + 
         COALESCE(reading_3, 0) + COALESCE(reading_4, 0) + 
         COALESCE(reading_5, 0)) /
        NULLIF(
            CASE WHEN reading_1 IS NOT NULL THEN 1 ELSE 0 END +
            CASE WHEN reading_2 IS NOT NULL THEN 1 ELSE 0 END +
            CASE WHEN reading_3 IS NOT NULL THEN 1 ELSE 0 END +
            CASE WHEN reading_4 IS NOT NULL THEN 1 ELSE 0 END +
            CASE WHEN reading_5 IS NOT NULL THEN 1 ELSE 0 END, 0
        )
    ) STORED,
    
    -- Results
    result VARCHAR(20), -- 'PASS', 'FAIL', 'N/A'
    notes TEXT,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    UNIQUE(inspection_id, item_sequence)
);

-- Non-Conformance Reports (NCR)
CREATE TABLE non_conformance_reports (
    ncr_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    ncr_number VARCHAR(50) NOT NULL,
    status quality_status NOT NULL DEFAULT 'DRAFT',
    
    -- Reference Information
    reference_type VARCHAR(50), -- 'INSPECTION', 'CUSTOMER_COMPLAINT', 'INTERNAL_AUDIT', etc.
    reference_id INT,
    reference_document VARCHAR(100),
    
    -- Product/Item Information
    product_id INT REFERENCES products(product_id) ON DELETE SET NULL,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    product_name VARCHAR(255),
    product_sku VARCHAR(100),
    lot_number VARCHAR(100),
    
    -- Non-Conformance Details
    non_conformance_date DATE NOT NULL,
    detected_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20), -- 'CRITICAL', 'MAJOR', 'MINOR'
    category VARCHAR(50), -- 'MATERIAL', 'PROCESS', 'DESIGN', 'VENDOR', etc.
    
    -- Containment Actions
    containment_action TEXT,
    containment_required_by DATE,
    containment_completed_date DATE,
    
    -- Root Cause Analysis
    root_cause_analysis TEXT,
    root_cause_analysis_date DATE,
    root_cause_analysis_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Corrective Actions
    corrective_action TEXT,
    corrective_action_required_by DATE,
    corrective_action_completed_date DATE,
    
    -- Preventive Actions
    preventive_action TEXT,
    preventive_action_required_by DATE,
    preventive_action_completed_date DATE,
    
    -- Verification
    verification_details TEXT,
    verified_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    verification_date DATE,
    
    -- Closure
    closure_notes TEXT,
    closed_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    closed_date DATE,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, ncr_number)
);

-- Quality Alerts
CREATE TABLE quality_alerts (
    alert_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    alert_number VARCHAR(50) NOT NULL,
    status quality_status NOT NULL DEFAULT 'DRAFT',
    
    -- Alert Information
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(20), -- 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    target_resolution_date DATE,
    actual_resolution_date DATE,
    
    -- Reference Information
    reference_type VARCHAR(50), -- 'NCR', 'INSPECTION', 'CUSTOMER_COMPLAINT', etc.
    reference_id INT,
    reference_document VARCHAR(100),
    
    -- Product/Process Information
    product_id INT REFERENCES products(product_id) ON DELETE SET NULL,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    process_id INT, -- Reference to manufacturing process if applicable
    
    -- Assignment
    assigned_to INT REFERENCES users(user_id) ON DELETE SET NULL,
    assigned_date DATE,
    
    -- Resolution
    resolution_notes TEXT,
    resolved_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    resolved_date DATE,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Constraints
    UNIQUE(tenant_id, alert_number)
);

-- Audit Triggers
CREATE TRIGGER set_quality_standards_updated_at
BEFORE UPDATE ON quality_standards
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_quality_inspection_plans_updated_at
BEFORE UPDATE ON quality_inspection_plans
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_quality_inspection_plan_items_updated_at
BEFORE UPDATE ON quality_inspection_plan_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_quality_inspections_updated_at
BEFORE UPDATE ON quality_inspections
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_quality_inspection_items_updated_at
BEFORE UPDATE ON quality_inspection_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_non_conformance_reports_updated_at
BEFORE UPDATE ON non_conformance_reports
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_quality_alerts_updated_at
BEFORE UPDATE ON quality_alerts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Function to create inspection from plan
CREATE OR REPLACE FUNCTION create_inspection_from_plan(
    p_plan_id INT,
    p_reference_type VARCHAR(50),
    p_reference_id INT,
    p_reference_document VARCHAR(100),
    p_product_id INT,
    p_variant_id INT,
    p_quantity NUMERIC(12,4),
    p_planned_date DATE,
    p_created_by INT
)
RETURNS INT AS $$
DECLARE
    v_tenant_id INT;
    v_inspection_id INT;
    v_inspection_number VARCHAR(50);
    v_plan RECORD;
    v_product RECORD;
    v_item_count INT;
BEGIN
    -- Get plan details
    SELECT p.*, t.tenant_id
    INTO v_plan
    FROM quality_inspection_plans p
    JOIN tenants t ON t.tenant_id = p.tenant_id
    WHERE p.plan_id = p_plan_id
    AND p.is_active = TRUE;
    
    IF v_plan.plan_id IS NULL THEN
        RAISE EXCEPTION 'Inspection plan not found or inactive';
    END IF;
    
    -- Get product details
    SELECT product_name, product_code
    INTO v_product
    FROM products
    WHERE product_id = p_product_id;
    
    IF v_product.product_name IS NULL THEN
        RAISE EXCEPTION 'Product not found';
    END IF;
    
    -- Generate inspection number
    SELECT 'QI-' || to_char(CURRENT_DATE, 'YYYYMM') || '-' || 
           lpad((COALESCE(MAX(SUBSTRING(inspection_number, 'QI-\d{6}-(\d+)')::INT), 0) + 1)::TEXT, 5, '0')
    INTO v_inspection_number
    FROM quality_inspections
    WHERE inspection_number ~ ('^QI-' || to_char(CURRENT_DATE, 'YYYYMM') || '-\d+$')
    AND tenant_id = v_plan.tenant_id;
    
    -- Create inspection header
    INSERT INTO quality_inspections (
        tenant_id, inspection_number, inspection_type, status,
        reference_type, reference_id, reference_document,
        product_id, variant_id, product_name, product_sku,
        planned_date, planned_by, created_by, updated_by,
        quantity_inspected
    ) VALUES (
        v_plan.tenant_id, v_inspection_number, v_plan.inspection_type, 'PENDING',
        p_reference_type, p_reference_id, p_reference_document,
        p_product_id, p_variant_id, v_product.product_name, v_product.product_code,
        p_planned_date, p_created_by, p_created_by, p_created_by,
        p_quantity
    )
    RETURNING inspection_id INTO v_inspection_id;
    
    -- Add inspection items from plan
    INSERT INTO quality_inspection_items (
        inspection_id, plan_item_id, item_sequence,
        check_type, description, specification, method
    )
    SELECT 
        v_inspection_id, pi.plan_item_id, pi.item_sequence,
        pi.check_type, pi.description, pi.specification, pi.method
    FROM quality_inspection_plan_items pi
    WHERE pi.plan_id = p_plan_id
    ORDER BY pi.item_sequence;
    
    -- Update item count
    GET DIAGNOSTICS v_item_count = ROW_COUNT;
    
    -- If no items were added, delete the inspection
    IF v_item_count = 0 THEN
        DELETE FROM quality_inspections WHERE inspection_id = v_inspection_id;
        RAISE EXCEPTION 'No inspection items found in the plan';
    END IF;
    
    RETURN v_inspection_id;
END;
$$ LANGUAGE plpgsql;

-- Function to complete inspection
CREATE OR REPLACE FUNCTION complete_inspection(
    p_inspection_id INT,
    p_result VARCHAR(20),
    p_notes TEXT,
    p_completed_by INT
)
RETURNS VOID AS $$
DECLARE
    v_inspection RECORD;
    v_failed_items INT;
    v_passed_items INT;
    v_total_items INT;
BEGIN
    -- Get inspection details
    SELECT i.*, t.tenant_id
    INTO v_inspection
    FROM quality_inspections i
    JOIN tenants t ON t.tenant_id = i.tenant_id
    WHERE i.inspection_id = p_inspection_id
    AND i.status = 'IN_PROGRESS';
    
    IF v_inspection.inspection_id IS NULL THEN
        RAISE EXCEPTION 'Inspection not found or not in progress';
    END IF;
    
    -- Check if all items have results
    SELECT 
        COUNT(*) FILTER (WHERE result IS NULL) AS missing_results,
        COUNT(*) FILTER (WHERE result = 'FAIL') AS failed_items,
        COUNT(*) AS total_items
    INTO v_failed_items, v_passed_items, v_total_items
    FROM quality_inspection_items
    WHERE inspection_id = p_inspection_id;
    
    IF v_missing_results > 0 THEN
        RAISE EXCEPTION 'Not all inspection items have results';
    END IF;
    
    -- Update inspection status and results
    UPDATE quality_inspections
    SET 
        status = CASE 
            WHEN p_result = 'PASS' THEN 'PASSED'
            WHEN p_result = 'FAIL' AND v_failed_items > 0 THEN 'FAILED'
            WHEN p_result = 'QUARANTINE' THEN 'QUARANTINED'
            ELSE 'COMPLETED'
        END,
        result = p_result,
        result_details = jsonb_build_object(
            'total_items', v_total_items,
            'passed_items', (v_total_items - v_failed_items),
            'failed_items', v_failed_items,
            'pass_percentage', ROUND(((v_total_items - v_failed_items) * 100.0) / NULLIF(v_total_items, 0), 2)
        ),
        inspected_by = p_completed_by,
        actual_date = CURRENT_TIMESTAMP,
        updated_by = p_completed_by,
        updated_at = CURRENT_TIMESTAMP,
        notes = p_notes
    WHERE inspection_id = p_inspection_id;
    
    -- If failed, create NCR
    IF p_result = 'FAIL' AND v_failed_items > 0 THEN
        INSERT INTO non_conformance_reports (
            tenant_id, ncr_number, status,
            reference_type, reference_id, reference_document,
            product_id, variant_id, product_name, product_sku,
            non_conformance_date, detected_by, description,
            severity, category, created_by, updated_by
        )
        VALUES (
            v_inspection.tenant_id,
            'NCR-' || to_char(CURRENT_DATE, 'YYYYMM') || '-' || 
            lpad((COALESCE(MAX(SUBSTRING(ncr_number, 'NCR-\d{6}-(\d+)')::INT), 0) + 1)::TEXT, 5, '0'),
            'OPEN',
            'INSPECTION', p_inspection_id, v_inspection.inspection_number,
            v_inspection.product_id, v_inspection.variant_id, v_inspection.product_name, v_inspection.product_sku,
            CURRENT_DATE, p_completed_by, 
            'Quality inspection ' || v_inspection.inspection_number || ' failed with ' || v_failed_items || ' non-conformances',
            'MAJOR', 'QUALITY', p_completed_by, p_completed_by
        )
        FROM non_conformance_reports
        WHERE ncr_number ~ ('^NCR-' || to_char(CURRENT_DATE, 'YYYYMM') || '-\d+$')
        AND tenant_id = v_inspection.tenant_id
        ORDER BY ncr_number DESC
        LIMIT 1;
    END IF;
END;
$$ LANGUAGE plpgsql;