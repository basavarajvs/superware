-- ===================================================
-- MODULE: GEOGRAPHY
-- ===================================================

-- Countries
CREATE TABLE countries (
    country_id SERIAL PRIMARY KEY,
    country_name VARCHAR(100) NOT NULL UNIQUE,
    country_code_iso2 CHAR(2) NOT NULL UNIQUE,
    country_code_iso3 CHAR(3) NOT NULL UNIQUE,
    phone_code VARCHAR(10),
    currency_code CHAR(3),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_countries_updated_at
BEFORE UPDATE ON countries
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- States/Provinces
CREATE TABLE states (
    state_id SERIAL PRIMARY KEY,
    state_name VARCHAR(100) NOT NULL,
    state_code VARCHAR(10),
    country_id INT NOT NULL REFERENCES countries(country_id) ON DELETE CASCADE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(country_id, state_name)
);

CREATE TRIGGER set_states_updated_at
BEFORE UPDATE ON states
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Cities
CREATE TABLE cities (
    city_id SERIAL PRIMARY KEY,
    city_name VARCHAR(100) NOT NULL,
    state_id INT NOT NULL REFERENCES states(state_id) ON DELETE CASCADE,
    zip_code VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(state_id, city_name, zip_code)
);

CREATE TRIGGER set_cities_updated_at
BEFORE UPDATE ON cities
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Time zones
CREATE TABLE timezones (
    timezone_id SERIAL PRIMARY KEY,
    timezone_name VARCHAR(50) NOT NULL UNIQUE,
    utc_offset INTERVAL,
    is_dst BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Insert common countries, states, and timezones
-- Note: This is a minimal set for demonstration. In production, you would import a full dataset.

-- Sample countries
INSERT INTO countries (country_name, country_code_iso2, country_code_iso3, phone_code, currency_code) VALUES
('United States', 'US', 'USA', '+1', 'USD'),
('United Kingdom', 'GB', 'GBR', '+44', 'GBP'),
('Canada', 'CA', 'CAN', '+1', 'CAD'),
('Germany', 'DE', 'DEU', '+49', 'EUR'),
('France', 'FR', 'FRA', '+33', 'EUR'),
('Japan', 'JP', 'JPN', '+81', 'JPY'),
('Australia', 'AU', 'AUS', '+61', 'AUD'),
('China', 'CN', 'CHN', '+86', 'CNY'),
('India', 'IN', 'IND', '+91', 'INR'),
('Brazil', 'BR', 'BRA', '+55', 'BRL');

-- Sample US states
INSERT INTO states (state_name, state_code, country_id) 
SELECT 'California', 'CA', country_id FROM countries WHERE country_code_iso2 = 'US';
INSERT INTO states (state_name, state_code, country_id) 
SELECT 'New York', 'NY', country_id FROM countries WHERE country_code_iso2 = 'US';
INSERT INTO states (state_name, state_code, country_id) 
SELECT 'Texas', 'TX', country_id FROM countries WHERE country_code_iso2 = 'US';

-- Sample timezones
INSERT INTO timezones (timezone_name, utc_offset, is_dst) VALUES
('UTC', '00:00:00', FALSE),
('America/New_York', '-05:00:00', TRUE),
('America/Chicago', '-06:00:00', TRUE),
('America/Denver', '-07:00:00', TRUE),
('America/Los_Angeles', '-08:00:00', TRUE),
('Europe/London', '+00:00:00', TRUE),
('Europe/Paris', '+01:00:00', TRUE),
('Asia/Tokyo', '+09:00:00', FALSE),
('Asia/Shanghai', '+08:00:00', FALSE),
('Australia/Sydney', '+10:00:00', TRUE);