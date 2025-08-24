-- ===================================================
-- UTILITY FUNCTIONS & TRIGGERS
-- ===================================================

-- Trigger function to update the 'updated_at' timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to generate tenant-specific sequence numbers
CREATE OR REPLACE FUNCTION generate_tenant_sequence(tenant_id INT, prefix TEXT, sequence_name TEXT)
RETURNS TEXT AS $$
DECLARE
    next_val BIGINT;
    result TEXT;
BEGIN
    -- Create sequence if it doesn't exist
    EXECUTE format('CREATE SEQUENCE IF NOT EXISTS tenant_%s_%s START 1', tenant_id, sequence_name);
    
    -- Get next value
    EXECUTE format('SELECT nextval(''tenant_%s_%s'')', tenant_id, sequence_name) INTO next_val;
    
    -- Format result
    result := prefix || '-' || tenant_id || '-' || next_val;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function to check tenant access for a user
CREATE OR REPLACE FUNCTION check_tenant_access(user_id INT, tenant_id INT)
RETURNS BOOLEAN AS $$
DECLARE
    access_count INT;
BEGIN
    SELECT COUNT(*) INTO access_count
    FROM users u
    WHERE u.user_id = check_tenant_access.user_id 
    AND u.tenant_id = check_tenant_access.tenant_id 
    AND u.is_active = TRUE;
    
    RETURN access_count > 0;
END;
$$ LANGUAGE plpgsql;

-- Function to check if a user has a specific role
CREATE OR REPLACE FUNCTION check_user_role(user_id INT, role_name TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    role_count INT;
BEGIN
    SELECT COUNT(*) INTO role_count
    FROM users u
    JOIN user_roles ur ON u.user_id = ur.user_id
    JOIN roles r ON ur.role_id = r.role_id
    WHERE u.user_id = check_user_role.user_id 
    AND r.role_name = check_user_role.role_name;
    
    RETURN role_count > 0;
END;
$$ LANGUAGE plpgsql;