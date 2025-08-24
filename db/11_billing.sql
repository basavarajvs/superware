-- ===================================================
-- MODULE: BILLING & LICENSING
-- ===================================================

-- Subscription status
CREATE TYPE subscription_status AS ENUM('ACTIVE', 'SUSPENDED', 'CANCELLED', 'EXPIRED');

-- Payment frequency
CREATE TYPE payment_frequency AS ENUM('MONTHLY', 'QUARTERLY', 'ANNUALLY');

-- Invoice status
CREATE TYPE billing_invoice_status AS ENUM('DRAFT', 'ISSUED', 'PAID', 'OVERDUE', 'CANCELLED');

-- Payment status
CREATE TYPE billing_payment_status AS ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');

-- Tenant subscriptions
CREATE TABLE tenant_subscriptions (
    subscription_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    plan_id INT NOT NULL REFERENCES license_plans(plan_id) ON DELETE CASCADE,
    
    -- Subscription Information
    subscription_start_date DATE NOT NULL,
    subscription_end_date DATE NOT NULL,
    payment_frequency payment_frequency DEFAULT 'MONTHLY',
    next_billing_date DATE,
    
    -- Status Information
    subscription_status subscription_status DEFAULT 'ACTIVE',
    suspended_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    cancelled_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Usage Tracking
    current_users INT DEFAULT 0,
    current_warehouses INT DEFAULT 0,
    current_monthly_transactions INT DEFAULT 0,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TRIGGER set_tenant_subscriptions_updated_at
BEFORE UPDATE ON tenant_subscriptions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Subscription invoices
CREATE TABLE subscription_invoices (
    invoice_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    subscription_id INT NOT NULL REFERENCES tenant_subscriptions(subscription_id) ON DELETE CASCADE,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Invoice Information
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    billing_period_start DATE NOT NULL,
    billing_period_end DATE NOT NULL,
    
    -- Financial Information
    currency_code CHAR(3) DEFAULT 'USD',
    plan_amount NUMERIC(12,2) NOT NULL,
    setup_fee_amount NUMERIC(12,2) DEFAULT 0.00,
    discount_amount NUMERIC(12,2) DEFAULT 0.00,
    tax_amount NUMERIC(12,2) DEFAULT 0.00,
    total_amount NUMERIC(12,2) NOT NULL, -- Computed as (plan + setup - discount + tax)
    amount_paid NUMERIC(12,2) DEFAULT 0.00,
    balance_due NUMERIC(12,2) NOT NULL, -- Computed as (total - paid)
    
    -- Status Information
    invoice_status billing_invoice_status DEFAULT 'ISSUED',
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_subscription_invoices_updated_at
BEFORE UPDATE ON subscription_invoices
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Subscription payments
CREATE TABLE subscription_payments (
    payment_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    invoice_id INT REFERENCES subscription_invoices(invoice_id) ON DELETE SET NULL,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Payment Information
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL, -- CREDIT_CARD, BANK_TRANSFER, etc.
    payment_reference VARCHAR(100), -- Transaction ID, check number, etc.
    
    -- Financial Information
    currency_code CHAR(3) DEFAULT 'USD',
    payment_amount NUMERIC(12,2) NOT NULL,
    
    -- Status Information
    payment_status billing_payment_status DEFAULT 'COMPLETED',
    processed_at TIMESTAMPTZ,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_subscription_payments_updated_at
BEFORE UPDATE ON subscription_payments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Usage tracking
CREATE TABLE usage_tracking (
    usage_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    subscription_id INT NOT NULL REFERENCES tenant_subscriptions(subscription_id) ON DELETE CASCADE,
    usage_date DATE NOT NULL,
    
    -- Usage Metrics
    user_count INT DEFAULT 0,
    warehouse_count INT DEFAULT 0,
    transaction_count INT DEFAULT 0,
    
    -- Status and Metadata
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, subscription_id, usage_date)
);

CREATE TRIGGER set_usage_tracking_updated_at
BEFORE UPDATE ON usage_tracking
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Payment methods
CREATE TABLE payment_methods (
    method_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Payment Method Information
    method_type VARCHAR(50) NOT NULL, -- CREDIT_CARD, BANK_ACCOUNT, etc.
    method_name VARCHAR(100), -- e.g., "Visa ending in 1234"
    is_default BOOLEAN DEFAULT FALSE,
    
    -- Credit Card Specific Fields
    card_brand VARCHAR(20),
    card_last_four VARCHAR(4),
    card_expiry_month INT,
    card_expiry_year INT,
    
    -- Bank Account Specific Fields
    bank_name VARCHAR(100),
    bank_account_last_four VARCHAR(4),
    
    -- Status Information
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_payment_methods_updated_at
BEFORE UPDATE ON payment_methods
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Refunds
CREATE TABLE refunds (
    refund_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    payment_id INT NOT NULL REFERENCES subscription_payments(payment_id) ON DELETE CASCADE,
    refund_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Refund Information
    refund_date DATE NOT NULL,
    refund_reason VARCHAR(200),
    
    -- Financial Information
    currency_code CHAR(3) DEFAULT 'USD',
    refund_amount NUMERIC(12,2) NOT NULL,
    
    -- Status Information
    refund_status billing_payment_status DEFAULT 'COMPLETED',
    processed_at TIMESTAMPTZ,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_refunds_updated_at
BEFORE UPDATE ON refunds
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Discounts and coupons
CREATE TABLE discounts (
    discount_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    discount_code VARCHAR(50) UNIQUE,
    
    -- Discount Information
    discount_name VARCHAR(100) NOT NULL,
    discount_type VARCHAR(20) NOT NULL, -- PERCENTAGE, FIXED_AMOUNT
    discount_value NUMERIC(10,2) NOT NULL, -- Percentage or fixed amount
    
    -- Usage Limits
    max_uses INT,
    current_uses INT DEFAULT 0,
    max_uses_per_customer INT,
    
    -- Validity Period
    valid_from DATE,
    valid_until DATE,
    
    -- Status Information
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_discounts_updated_at
BEFORE UPDATE ON discounts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Discount applications
CREATE TABLE discount_applications (
    application_id SERIAL PRIMARY KEY,
    discount_id INT NOT NULL REFERENCES discounts(discount_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    subscription_id INT REFERENCES tenant_subscriptions(subscription_id) ON DELETE SET NULL,
    
    -- Application Information
    applied_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    applied_amount NUMERIC(12,2) NOT NULL,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX idx_tenant_subscriptions_tenant ON tenant_subscriptions(tenant_id);
CREATE INDEX idx_tenant_subscriptions_plan ON tenant_subscriptions(plan_id);
CREATE INDEX idx_tenant_subscriptions_status ON tenant_subscriptions(subscription_status);
CREATE INDEX idx_subscription_invoices_tenant ON subscription_invoices(tenant_id);
CREATE INDEX idx_subscription_invoices_subscription ON subscription_invoices(subscription_id);
CREATE INDEX idx_subscription_invoices_status ON subscription_invoices(invoice_status);
CREATE INDEX idx_subscription_payments_tenant ON subscription_payments(tenant_id);
CREATE INDEX idx_subscription_payments_invoice ON subscription_payments(invoice_id);
CREATE INDEX idx_subscription_payments_status ON subscription_payments(payment_status);
CREATE INDEX idx_usage_tracking_tenant ON usage_tracking(tenant_id);
CREATE INDEX idx_usage_tracking_subscription ON usage_tracking(subscription_id);
CREATE INDEX idx_usage_tracking_date ON usage_tracking(usage_date);
CREATE INDEX idx_payment_methods_tenant ON payment_methods(tenant_id);
CREATE INDEX idx_payment_methods_default ON payment_methods(is_default);
CREATE INDEX idx_refunds_tenant ON refunds(tenant_id);
CREATE INDEX idx_refunds_payment ON refunds(payment_id);
CREATE INDEX idx_refunds_status ON refunds(refund_status);
CREATE INDEX idx_discounts_tenant ON discounts(tenant_id);
CREATE INDEX idx_discounts_code ON discounts(discount_code);
CREATE INDEX idx_discounts_active ON discounts(is_active);
CREATE INDEX idx_discount_applications_discount ON discount_applications(discount_id);
CREATE INDEX idx_discount_applications_subscription ON discount_applications(subscription_id);