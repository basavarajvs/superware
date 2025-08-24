-- ===================================================
-- MODULE: PRODUCTS
-- ===================================================

-- Product categories
CREATE TABLE product_categories (
    category_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    category_name VARCHAR(100) NOT NULL,
    category_code VARCHAR(50) UNIQUE,
    parent_category_id INT REFERENCES product_categories(category_id) ON DELETE SET NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_product_categories_updated_at
BEFORE UPDATE ON product_categories
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Product brands
CREATE TABLE product_brands (
    brand_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    brand_name VARCHAR(100) NOT NULL,
    brand_code VARCHAR(50) UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_product_brands_updated_at
BEFORE UPDATE ON product_brands
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Products
CREATE TABLE products (
    product_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    product_name VARCHAR(200) NOT NULL,
    product_code VARCHAR(50) UNIQUE,
    description TEXT,
    category_id INT REFERENCES product_categories(category_id) ON DELETE SET NULL,
    brand_id INT REFERENCES product_brands(brand_id) ON DELETE SET NULL,
    unit_of_measure VARCHAR(20) DEFAULT 'EA', -- EA (Each), KG (Kilogram), L (Liter), M (Meter), etc.
    weight_kg NUMERIC(10, 4),
    volume_m3 NUMERIC(10, 4),
    length_m NUMERIC(10, 4),
    width_m NUMERIC(10, 4),
    height_m NUMERIC(10, 4),
    is_hazardous BOOLEAN DEFAULT FALSE,
    is_perishable BOOLEAN DEFAULT FALSE,
    is_serialized BOOLEAN DEFAULT FALSE,
    is_lot_tracked BOOLEAN DEFAULT FALSE,
    requires_temperature_control BOOLEAN DEFAULT FALSE,
    storage_temperature_min NUMERIC(5, 2),
    storage_temperature_max NUMERIC(5, 2),
    shelf_life_days INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Product variants (SKUs)
CREATE TABLE product_variants (
    variant_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_name VARCHAR(200),
    sku VARCHAR(50) NOT NULL UNIQUE,
    barcode VARCHAR(100),
    upc VARCHAR(12),
    ean VARCHAR(13),
    weight_kg NUMERIC(10, 4),
    volume_m3 NUMERIC(10, 4),
    length_m NUMERIC(10, 4),
    width_m NUMERIC(10, 4),
    height_m NUMERIC(10, 4),
    cost_price NUMERIC(18, 4),
    selling_price NUMERIC(18, 4),
    minimum_reorder_qty NUMERIC(12, 4) DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_product_variants_updated_at
BEFORE UPDATE ON product_variants
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Product attributes
CREATE TABLE product_attributes (
    attribute_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    attribute_name VARCHAR(100) NOT NULL,
    attribute_code VARCHAR(50) UNIQUE,
    attribute_type VARCHAR(20) DEFAULT 'TEXT', -- TEXT, NUMBER, DATE, BOOLEAN
    is_required BOOLEAN DEFAULT FALSE,
    is_searchable BOOLEAN DEFAULT TRUE,
    is_filterable BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_product_attributes_updated_at
BEFORE UPDATE ON product_attributes
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Product attribute values
CREATE TABLE product_attribute_values (
    value_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    attribute_id INT NOT NULL REFERENCES product_attributes(attribute_id) ON DELETE CASCADE,
    attribute_value TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(product_id, attribute_id)
);

CREATE TRIGGER set_product_attribute_values_updated_at
BEFORE UPDATE ON product_attribute_values
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Product images
CREATE TABLE product_images (
    image_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_product_images_updated_at
BEFORE UPDATE ON product_images
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Product suppliers
CREATE TABLE product_suppliers (
    product_supplier_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    vendor_id INT NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    supplier_product_code VARCHAR(50),
    supplier_product_name VARCHAR(200),
    cost_price NUMERIC(18, 4),
    lead_time_days INT,
    min_order_qty NUMERIC(12, 4),
    is_preferred BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(product_id, vendor_id)
);

CREATE TRIGGER set_product_suppliers_updated_at
BEFORE UPDATE ON product_suppliers
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Indexes for better performance
CREATE INDEX idx_product_categories_tenant ON product_categories(tenant_id);
CREATE INDEX idx_products_tenant ON products(tenant_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_product_variants_product ON product_variants(product_id);
CREATE INDEX idx_product_variants_sku ON product_variants(sku);
CREATE INDEX idx_product_attributes_tenant ON product_attributes(tenant_id);
CREATE INDEX idx_product_attribute_values_product ON product_attribute_values(product_id);
CREATE INDEX idx_product_suppliers_product ON product_suppliers(product_id);