-- ===================================================
-- MODULE: ACCOUNTING
-- ===================================================

-- Account types
CREATE TYPE account_type AS ENUM('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE');

-- Account categories
CREATE TYPE account_category AS ENUM('CURRENT_ASSET', 'FIXED_ASSET', 'CURRENT_LIABILITY', 'LONG_TERM_LIABILITY', 'EQUITY', 'REVENUE', 'COST_OF_SALES', 'OPERATING_EXPENSE', 'OTHER_INCOME', 'OTHER_EXPENSE');

-- Journal entry status
CREATE TYPE journal_status AS ENUM('DRAFT', 'POSTED', 'CANCELLED');

-- Payment methods
CREATE TYPE payment_method AS ENUM('CASH', 'CHECK', 'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'OTHER');

-- Payment status
CREATE TYPE payment_status AS ENUM('PENDING', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED');

-- Chart of accounts
CREATE TABLE chart_of_accounts (
    account_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    account_code VARCHAR(20) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    account_type account_type NOT NULL,
    account_category account_category NOT NULL,
    parent_account_id INT REFERENCES chart_of_accounts(account_id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(tenant_id, account_code)
);

CREATE TRIGGER set_chart_of_accounts_updated_at
BEFORE UPDATE ON chart_of_accounts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Journal entries
CREATE TABLE journal_entries (
    entry_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    entry_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Entry Information
    entry_date DATE NOT NULL,
    reference_number VARCHAR(50),
    reference_type VARCHAR(20), -- SALES_ORDER, PURCHASE_ORDER, etc.
    reference_id INT,
    
    -- Status Information
    journal_status journal_status DEFAULT 'DRAFT',
    posted_at TIMESTAMPTZ,
    posted_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Financial Information
    total_debit NUMERIC(18,2) DEFAULT 0.00,
    total_credit NUMERIC(18,2) DEFAULT 0.00,
    currency_code CHAR(3) DEFAULT 'USD',
    
    -- Status and Metadata
    description TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_journal_entries_updated_at
BEFORE UPDATE ON journal_entries
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Journal entry lines
CREATE TABLE journal_entry_lines (
    line_id SERIAL PRIMARY KEY,
    entry_id INT NOT NULL REFERENCES journal_entries(entry_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Account Information
    account_id INT NOT NULL REFERENCES chart_of_accounts(account_id) ON DELETE CASCADE,
    
    -- Amount Information
    debit_amount NUMERIC(18,2) DEFAULT 0.00,
    credit_amount NUMERIC(18,2) DEFAULT 0.00,
    
    -- Reference Information
    reference_type VARCHAR(20), -- INVENTORY_TRANSACTION, etc.
    reference_id INT,
    
    -- Status and Metadata
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_journal_entry_lines_updated_at
BEFORE UPDATE ON journal_entry_lines
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Customer invoices
CREATE TABLE customer_invoices (
    invoice_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    order_id INT REFERENCES sales_orders(order_id) ON DELETE SET NULL,
    client_id INT NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
    
    -- Invoice Information
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    payment_terms VARCHAR(50) DEFAULT 'NET_30',
    
    -- Financial Information
    currency_code CHAR(3) DEFAULT 'USD',
    subtotal NUMERIC(18,2) DEFAULT 0.00,
    tax_amount NUMERIC(18,2) DEFAULT 0.00,
    discount_amount NUMERIC(18,2) DEFAULT 0.00,
    total_amount NUMERIC(18,2) DEFAULT 0.00,
    amount_paid NUMERIC(18,2) DEFAULT 0.00,
    balance_due NUMERIC(18,2) DEFAULT 0.00, -- Computed as (total - paid)
    
    -- Status Information
    payment_status payment_status DEFAULT 'PENDING',
    invoice_status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, CLOSED, VOID
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_customer_invoices_updated_at
BEFORE UPDATE ON customer_invoices
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Customer invoice lines
CREATE TABLE customer_invoice_lines (
    line_id SERIAL PRIMARY KEY,
    invoice_id INT NOT NULL REFERENCES customer_invoices(invoice_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Reference Information
    order_item_id INT REFERENCES sales_order_items(order_item_id) ON DELETE SET NULL,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    
    -- Quantity and Pricing
    quantity NUMERIC(12,4) NOT NULL,
    unit_price NUMERIC(18,4) NOT NULL,
    tax_rate NUMERIC(5,2) DEFAULT 0.00,
    discount_percent NUMERIC(5,2) DEFAULT 0.00,
    
    -- Amounts
    line_total NUMERIC(18,2) NOT NULL, -- Computed as (quantity * unit_price * (1 - discount/100) * (1 + tax/100))
    
    -- Status and Metadata
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_customer_invoice_lines_updated_at
BEFORE UPDATE ON customer_invoice_lines
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Vendor bills
CREATE TABLE vendor_bills (
    bill_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    bill_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    po_id INT REFERENCES purchase_orders(po_id) ON DELETE SET NULL,
    vendor_id INT NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    
    -- Bill Information
    bill_date DATE NOT NULL,
    due_date DATE NOT NULL,
    payment_terms VARCHAR(50) DEFAULT 'NET_30',
    
    -- Financial Information
    currency_code CHAR(3) DEFAULT 'USD',
    subtotal NUMERIC(18,2) DEFAULT 0.00,
    tax_amount NUMERIC(18,2) DEFAULT 0.00,
    discount_amount NUMERIC(18,2) DEFAULT 0.00,
    total_amount NUMERIC(18,2) DEFAULT 0.00,
    amount_paid NUMERIC(18,2) DEFAULT 0.00,
    balance_due NUMERIC(18,2) DEFAULT 0.00, -- Computed as (total - paid)
    
    -- Status Information
    payment_status payment_status DEFAULT 'PENDING',
    bill_status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, CLOSED, VOID
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_vendor_bills_updated_at
BEFORE UPDATE ON vendor_bills
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Vendor bill lines
CREATE TABLE vendor_bill_lines (
    line_id SERIAL PRIMARY KEY,
    bill_id INT NOT NULL REFERENCES vendor_bills(bill_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Reference Information
    po_item_id INT REFERENCES purchase_order_items(po_item_id) ON DELETE SET NULL,
    
    -- Product Information
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variants(variant_id) ON DELETE SET NULL,
    
    -- Quantity and Pricing
    quantity NUMERIC(12,4) NOT NULL,
    unit_price NUMERIC(18,4) NOT NULL,
    tax_rate NUMERIC(5,2) DEFAULT 0.00,
    discount_percent NUMERIC(5,2) DEFAULT 0.00,
    
    -- Amounts
    line_total NUMERIC(18,2) NOT NULL, -- Computed as (quantity * unit_price * (1 - discount/100) * (1 + tax/100))
    
    -- Status and Metadata
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_vendor_bill_lines_updated_at
BEFORE UPDATE ON vendor_bill_lines
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Customer payments
CREATE TABLE customer_payments (
    payment_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    invoice_id INT REFERENCES customer_invoices(invoice_id) ON DELETE SET NULL,
    client_id INT NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
    
    -- Payment Information
    payment_date DATE NOT NULL,
    payment_method payment_method NOT NULL,
    payment_reference VARCHAR(100), -- Check number, transaction ID, etc.
    
    -- Financial Information
    currency_code CHAR(3) DEFAULT 'USD',
    payment_amount NUMERIC(18,2) NOT NULL,
    applied_amount NUMERIC(18,2) DEFAULT 0.00,
    unapplied_amount NUMERIC(18,2) NOT NULL, -- Computed as (payment - applied)
    
    -- Status Information
    payment_status payment_status DEFAULT 'PENDING',
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_customer_payments_updated_at
BEFORE UPDATE ON customer_payments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Customer payment applications
CREATE TABLE customer_payment_applications (
    application_id SERIAL PRIMARY KEY,
    payment_id INT NOT NULL REFERENCES customer_payments(payment_id) ON DELETE CASCADE,
    invoice_id INT NOT NULL REFERENCES customer_invoices(invoice_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Application Information
    applied_amount NUMERIC(18,2) NOT NULL,
    applied_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_customer_payment_applications_updated_at
BEFORE UPDATE ON customer_payment_applications
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Vendor payments
CREATE TABLE vendor_payments (
    payment_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    
    -- Reference Information
    bill_id INT REFERENCES vendor_bills(bill_id) ON DELETE SET NULL,
    vendor_id INT NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    
    -- Payment Information
    payment_date DATE NOT NULL,
    payment_method payment_method NOT NULL,
    payment_reference VARCHAR(100), -- Check number, transaction ID, etc.
    
    -- Financial Information
    currency_code CHAR(3) DEFAULT 'USD',
    payment_amount NUMERIC(18,2) NOT NULL,
    applied_amount NUMERIC(18,2) DEFAULT 0.00,
    unapplied_amount NUMERIC(18,2) NOT NULL, -- Computed as (payment - applied)
    
    -- Status Information
    payment_status payment_status DEFAULT 'PENDING',
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TRIGGER set_vendor_payments_updated_at
BEFORE UPDATE ON vendor_payments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Vendor payment applications
CREATE TABLE vendor_payment_applications (
    application_id SERIAL PRIMARY KEY,
    payment_id INT NOT NULL REFERENCES vendor_payments(payment_id) ON DELETE CASCADE,
    bill_id INT NOT NULL REFERENCES vendor_bills(bill_id) ON DELETE CASCADE,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    
    -- Application Information
    applied_amount NUMERIC(18,2) NOT NULL,
    applied_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    -- Status and Metadata
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_vendor_payment_applications_updated_at
BEFORE UPDATE ON vendor_payment_applications
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Financial periods
CREATE TABLE financial_periods (
    period_id SERIAL PRIMARY KEY,
    tenant_id INT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    period_name VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_closed BOOLEAN DEFAULT FALSE,
    closed_at TIMESTAMPTZ,
    closed_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_financial_periods_updated_at
BEFORE UPDATE ON financial_periods
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Indexes for better performance
CREATE INDEX idx_chart_of_accounts_tenant ON chart_of_accounts(tenant_id);
CREATE INDEX idx_chart_of_accounts_code ON chart_of_accounts(account_code);
CREATE INDEX idx_journal_entries_tenant ON journal_entries(tenant_id);
CREATE INDEX idx_journal_entries_date ON journal_entries(entry_date);
CREATE INDEX idx_journal_entries_status ON journal_entries(journal_status);
CREATE INDEX idx_journal_entry_lines_entry ON journal_entry_lines(entry_id);
CREATE INDEX idx_journal_entry_lines_account ON journal_entry_lines(account_id);
CREATE INDEX idx_customer_invoices_tenant ON customer_invoices(tenant_id);
CREATE INDEX idx_customer_invoices_client ON customer_invoices(client_id);
CREATE INDEX idx_customer_invoices_status ON customer_invoices(payment_status);
CREATE INDEX idx_customer_invoice_lines_invoice ON customer_invoice_lines(invoice_id);
CREATE INDEX idx_vendor_bills_tenant ON vendor_bills(tenant_id);
CREATE INDEX idx_vendor_bills_vendor ON vendor_bills(vendor_id);
CREATE INDEX idx_vendor_bills_status ON vendor_bills(payment_status);
CREATE INDEX idx_vendor_bill_lines_bill ON vendor_bill_lines(bill_id);
CREATE INDEX idx_customer_payments_tenant ON customer_payments(tenant_id);
CREATE INDEX idx_customer_payments_client ON customer_payments(client_id);
CREATE INDEX idx_customer_payments_status ON customer_payments(payment_status);
CREATE INDEX idx_customer_payment_applications_payment ON customer_payment_applications(payment_id);
CREATE INDEX idx_customer_payment_applications_invoice ON customer_payment_applications(invoice_id);
CREATE INDEX idx_vendor_payments_tenant ON vendor_payments(tenant_id);
CREATE INDEX idx_vendor_payments_vendor ON vendor_payments(vendor_id);
CREATE INDEX idx_vendor_payments_status ON vendor_payments(payment_status);
CREATE INDEX idx_vendor_payment_applications_payment ON vendor_payment_applications(payment_id);
CREATE INDEX idx_vendor_payment_applications_bill ON vendor_payment_applications(bill_id);
CREATE INDEX idx_financial_periods_tenant ON financial_periods(tenant_id);
CREATE INDEX idx_financial_periods_dates ON financial_periods(start_date, end_date);