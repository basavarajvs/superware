-- ===================================================
-- MODULE: CLIENTS & VENDORS
-- ===================================================

-- Client types
CREATE TYPE client_type AS ENUM('CUSTOMER', 'INTERNAL', 'PARTNER', 'OTHER');

-- Vendor types
CREATE TYPE vendor_type AS ENUM('SUPPLIER', 'TRANSPORTER', 'THIRD_PARTY_LOGISTICS', 'CUSTOMS_AGENT', 'OTHER');

-- Address types
CREATE TYPE address_type AS ENUM('BILLING', 'SHIPPING', 'HEADQUARTERS', 'WAREHOUSE', 'OTHER');

-- Clients (customers and business partners)
CREATE TABLE clients (
    client_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    client_name VARCHAR(100) NOT NULL,
    client_code VARCHAR(50) UNIQUE,
    client_type client_type DEFAULT 'CUSTOMER',
    parent_client_id INT REFERENCES clients(client_id) ON DELETE SET NULL,
    primary_contact_name VARCHAR(100),
    primary_contact_email VARCHAR(100),
    primary_contact_phone VARCHAR(20),
    secondary_contact_name VARCHAR(100),
    secondary_contact_email VARCHAR(100),
    secondary_contact_phone VARCHAR(20),
    website VARCHAR(255),
    tax_id_number VARCHAR(50),
    credit_limit NUMERIC(18, 4) DEFAULT 0.00,
    payment_terms VARCHAR(50) DEFAULT 'NET_30',
    currency_code CHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT TRUE,
    is_tax_exempt BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_clients_updated_at
BEFORE UPDATE ON clients
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Client addresses
CREATE TABLE client_addresses (
    address_id SERIAL PRIMARY KEY,
    client_id INT NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
    address_type address_type DEFAULT 'SHIPPING',
    address_name VARCHAR(100),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country_id INT REFERENCES countries(country_id) ON DELETE SET NULL,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_client_addresses_updated_at
BEFORE UPDATE ON client_addresses
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Vendors (suppliers and service providers)
CREATE TABLE vendors (
    vendor_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    vendor_name VARCHAR(100) NOT NULL,
    vendor_code VARCHAR(50) UNIQUE,
    vendor_type vendor_type,
    parent_vendor_id INT REFERENCES vendors(vendor_id) ON DELETE SET NULL,
    primary_contact_name VARCHAR(100),
    primary_contact_email VARCHAR(100),
    primary_contact_phone VARCHAR(20),
    secondary_contact_name VARCHAR(100),
    secondary_contact_email VARCHAR(100),
    secondary_contact_phone VARCHAR(20),
    website VARCHAR(255),
    tax_id_number VARCHAR(50),
    payment_terms VARCHAR(50) DEFAULT 'NET_30',
    currency_code CHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_vendors_updated_at
BEFORE UPDATE ON vendors
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Vendor addresses
CREATE TABLE vendor_addresses (
    address_id SERIAL PRIMARY KEY,
    vendor_id INT NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    address_type address_type DEFAULT 'SHIPPING',
    address_name VARCHAR(100),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country_id INT REFERENCES countries(country_id) ON DELETE SET NULL,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_vendor_addresses_updated_at
BEFORE UPDATE ON vendor_addresses
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Client contracts
CREATE TABLE client_contracts (
    contract_id SERIAL PRIMARY KEY,
    client_id INT NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
    contract_name VARCHAR(200) NOT NULL,
    contract_number VARCHAR(50) UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE,
    contract_value NUMERIC(18, 4),
    currency_code CHAR(3) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, EXPIRED, TERMINATED
    renewal_terms VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_client_contracts_updated_at
BEFORE UPDATE ON client_contracts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Vendor contracts
CREATE TABLE vendor_contracts (
    contract_id SERIAL PRIMARY KEY,
    vendor_id INT NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    contract_name VARCHAR(200) NOT NULL,
    contract_number VARCHAR(50) UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE,
    contract_value NUMERIC(18, 4),
    currency_code CHAR(3) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, EXPIRED, TERMINATED
    renewal_terms VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_vendor_contracts_updated_at
BEFORE UPDATE ON vendor_contracts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Indexes for better performance
CREATE INDEX idx_clients_tenant ON clients(tenant_id);
CREATE INDEX idx_clients_type ON clients(client_type);
CREATE INDEX idx_client_addresses_client ON client_addresses(client_id);
CREATE INDEX idx_vendors_tenant ON vendors(tenant_id);
CREATE INDEX idx_vendors_type ON vendors(vendor_type);
CREATE INDEX idx_vendor_addresses_vendor ON vendor_addresses(vendor_id);