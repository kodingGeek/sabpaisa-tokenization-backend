-- Platform table for multi-platform tokenization
CREATE TABLE platforms (
    id BIGSERIAL PRIMARY KEY,
    platform_code VARCHAR(50) UNIQUE NOT NULL,
    platform_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    icon_url VARCHAR(500),
    webhook_url VARCHAR(500),
    allowed_domains TEXT,
    merchant_id BIGINT REFERENCES merchants(id)
);

-- Token types table
CREATE TABLE token_types (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(20) UNIQUE NOT NULL,
    type_name VARCHAR(50) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    default_expiry_days INTEGER DEFAULT 365,
    max_tokens_per_card INTEGER DEFAULT 10,
    support_multiple_platforms BOOLEAN DEFAULT true,
    token_config TEXT
);

-- Pricing plans table
CREATE TABLE pricing_plans (
    id BIGSERIAL PRIMARY KEY,
    plan_code VARCHAR(50) UNIQUE NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    per_token_creation_price DECIMAL(10,2) DEFAULT 0.10,
    per_token_storage_price DECIMAL(10,2) DEFAULT 0.05,
    per_transaction_price DECIMAL(10,2) DEFAULT 0.02,
    per_platform_price DECIMAL(10,2) DEFAULT 500.00,
    free_tokens_per_month INTEGER DEFAULT 1000,
    max_tokens_per_month INTEGER DEFAULT 1000000,
    max_platforms INTEGER DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Enhanced tokens table (replacing original tokens table)
CREATE TABLE enhanced_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_value VARCHAR(255) UNIQUE NOT NULL,
    masked_pan VARCHAR(50) NOT NULL,
    card_hash VARCHAR(255) NOT NULL,
    card_bin VARCHAR(10) NOT NULL,
    card_last4 VARCHAR(4) NOT NULL,
    card_type VARCHAR(20),
    card_brand VARCHAR(20),
    merchant_id BIGINT NOT NULL REFERENCES merchants(id),
    platform_id BIGINT REFERENCES platforms(id),
    token_type_id BIGINT NOT NULL REFERENCES token_types(id),
    expiry_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    usage_count BIGINT DEFAULT 0,
    customer_email VARCHAR(255),
    customer_phone VARCHAR(20),
    customer_id VARCHAR(100),
    notification_enabled BOOLEAN DEFAULT true,
    days_before_expiry_notification INTEGER DEFAULT 30,
    last_notification_sent TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_chargeable BOOLEAN DEFAULT true,
    charged_at TIMESTAMP,
    CONSTRAINT idx_card_hash_platform UNIQUE (card_hash, platform_id)
);

-- Token usage tracking table
CREATE TABLE token_usage (
    id BIGSERIAL PRIMARY KEY,
    token_id BIGINT NOT NULL REFERENCES enhanced_tokens(id),
    merchant_id BIGINT NOT NULL REFERENCES merchants(id),
    platform_id BIGINT REFERENCES platforms(id),
    transaction_type VARCHAR(50) NOT NULL,
    usage_time TIMESTAMP NOT NULL,
    transaction_reference VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500)
);

-- Billing records table
CREATE TABLE billing_records (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES merchants(id),
    billing_month DATE NOT NULL,
    pricing_plan_id BIGINT NOT NULL REFERENCES pricing_plans(id),
    total_tokens_created BIGINT DEFAULT 0,
    total_active_tokens BIGINT DEFAULT 0,
    total_transactions BIGINT DEFAULT 0,
    token_creation_charges DECIMAL(10,2) DEFAULT 0,
    storage_charges DECIMAL(10,2) DEFAULT 0,
    transaction_charges DECIMAL(10,2) DEFAULT 0,
    platform_charges DECIMAL(10,2) DEFAULT 0,
    subtotal DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    due_date DATE,
    paid_date DATE,
    invoice_number VARCHAR(50),
    payment_reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_merchant_billing_month UNIQUE (merchant_id, billing_month)
);

-- Add pricing plan reference to merchants
ALTER TABLE merchants ADD COLUMN pricing_plan_id BIGINT REFERENCES pricing_plans(id);

-- Create indexes
CREATE INDEX idx_token_value ON enhanced_tokens(token_value);
CREATE INDEX idx_merchant_id ON enhanced_tokens(merchant_id);
CREATE INDEX idx_platform_id ON enhanced_tokens(platform_id);
CREATE INDEX idx_expiry_date ON enhanced_tokens(expiry_date);
CREATE INDEX idx_card_hash_platform ON enhanced_tokens(card_hash, platform_id);
CREATE INDEX idx_merchant_usage_time ON token_usage(merchant_id, usage_time);
CREATE INDEX idx_token_usage ON token_usage(token_id);
CREATE INDEX idx_platform_usage ON token_usage(platform_id);
CREATE INDEX idx_merchant_billing_month ON billing_records(merchant_id, billing_month);
CREATE INDEX idx_billing_status ON billing_records(status);

-- Insert default token types
INSERT INTO token_types (type_code, type_name, description, default_expiry_days) VALUES
    ('COF', 'Card on File', 'Standard tokenization for recurring payments', 365),
    ('FPT', 'Fast Payment Token', 'High-frequency low-value transactions', 180),
    ('OTT', 'One-Time Token', 'Single-use token for one transaction', 1),
    ('GUEST', 'Guest Checkout', 'Temporary token for guest users', 30),
    ('SUBSCRIPTION', 'Subscription', 'Long-term subscription payments', 730);

-- Insert default pricing plan
INSERT INTO pricing_plans (plan_code, plan_name, description) VALUES
    ('DEFAULT', 'Standard Plan', 'Default pricing plan for all merchants'),
    ('STARTER', 'Starter Plan', 'For small businesses with low volume'),
    ('GROWTH', 'Growth Plan', 'For growing businesses with moderate volume'),
    ('ENTERPRISE', 'Enterprise Plan', 'For large businesses with high volume');