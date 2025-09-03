-- Portfolio Database Schema
-- This script initializes the database schema for the portfolio management system

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create enum types
CREATE TYPE currency_type AS ENUM ('USD', 'EUR', 'GBP');
CREATE TYPE transaction_type AS ENUM ('BUY', 'SELL', 'DIVIDEND', 'SPLIT');

-- Create transactions table
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticker VARCHAR(20) NOT NULL,
    transaction_type transaction_type NOT NULL DEFAULT 'BUY',
    quantity DECIMAL(18, 6) NOT NULL,
    cost_per_share DECIMAL(18, 4) NOT NULL,
    currency currency_type NOT NULL,
    transaction_date DATE NOT NULL,
    commission DECIMAL(18, 4) DEFAULT 0.00,
    commission_currency currency_type,
    drip_confirmed BOOLEAN DEFAULT FALSE,
    is_fractional BOOLEAN DEFAULT FALSE,
    fractional_multiplier DECIMAL(10, 8) DEFAULT 1.0, -- TODO: use this when fractional transactions are detected to adjust the input market price
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create positions table (calculated/derived data)
CREATE TABLE positions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticker VARCHAR(20) UNIQUE NOT NULL,
    current_quantity DECIMAL(18, 6) NOT NULL,
    avg_cost_per_share DECIMAL(18, 4) NOT NULL,
    primary_currency currency_type NOT NULL,
    total_cost_basis DECIMAL(18, 4) NOT NULL,
    total_commissions DECIMAL(18, 4) DEFAULT 0.00,
    first_purchase_date DATE,
    last_transaction_date DATE,
    unrealized_gain_loss DECIMAL(18, 4) DEFAULT 0.00,
    current_market_value DECIMAL(18, 4) DEFAULT 0.00,
    current_price DECIMAL(18, 4) DEFAULT 0.00,
    last_price_update TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_transactions_ticker ON transactions(ticker);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_ticker_date ON transactions(ticker, transaction_date);
CREATE INDEX idx_positions_ticker ON positions(ticker);

-- Create a trigger to update the updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_positions_updated_at BEFORE UPDATE ON positions 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create a view for portfolio summary
CREATE VIEW portfolio_summary AS
SELECT 
    COUNT(*) as total_positions,
    SUM(current_quantity * current_price * 
        COALESCE((SELECT fractional_multiplier FROM transactions 
                 WHERE ticker = positions.ticker AND is_fractional = true 
                 ORDER BY transaction_date DESC LIMIT 1), 1.0)) as total_market_value,
    SUM(total_cost_basis) as total_cost_basis,
    SUM(current_quantity * current_price * 
        COALESCE((SELECT fractional_multiplier FROM transactions 
                 WHERE ticker = positions.ticker AND is_fractional = true 
                 ORDER BY transaction_date DESC LIMIT 1), 1.0)) - SUM(total_cost_basis) as total_unrealized_gain_loss,
    (SUM(current_quantity * current_price * 
         COALESCE((SELECT fractional_multiplier FROM transactions 
                  WHERE ticker = positions.ticker AND is_fractional = true 
                  ORDER BY transaction_date DESC LIMIT 1), 1.0)) - SUM(total_cost_basis)) / NULLIF(SUM(total_cost_basis), 0) * 100 as total_return_percentage
FROM positions 
WHERE current_quantity > 0;

-- Create a view for position details with calculations
CREATE VIEW position_details AS
SELECT 
    p.*,
    (p.current_quantity * p.current_price * 
     COALESCE((SELECT fractional_multiplier FROM transactions 
              WHERE ticker = p.ticker AND is_fractional = true 
              ORDER BY transaction_date DESC LIMIT 1), 1.0)) as market_value,
    (p.current_quantity * p.current_price * 
     COALESCE((SELECT fractional_multiplier FROM transactions 
              WHERE ticker = p.ticker AND is_fractional = true 
              ORDER BY transaction_date DESC LIMIT 1), 1.0)) - p.total_cost_basis as unrealized_pnl,
    CASE 
        WHEN p.total_cost_basis > 0 
        THEN ((p.current_quantity * p.current_price * 
               COALESCE((SELECT fractional_multiplier FROM transactions 
                        WHERE ticker = p.ticker AND is_fractional = true 
                        ORDER BY transaction_date DESC LIMIT 1), 1.0)) - p.total_cost_basis) / p.total_cost_basis * 100
        ELSE 0 
    END as return_percentage
FROM positions p
WHERE p.current_quantity > 0;

-- Create stored procedure to recalculate position from transactions
CREATE OR REPLACE FUNCTION recalculate_position(ticker_symbol VARCHAR(20))
RETURNS VOID AS $$
DECLARE
    position_data RECORD;
BEGIN
    -- Calculate position data from transactions
    SELECT 
        ticker_symbol as ticker,
        SUM(CASE WHEN transaction_type = 'BUY' THEN quantity ELSE -(quantity) END) as total_quantity,
        -- Calculate weighted average cost using actual shares
        (SUM(CASE WHEN transaction_type = 'BUY' THEN quantity * cost_per_share ELSE 0 END) + SUM(CASE WHEN transaction_type = 'BUY' THEN COALESCE(commission, 0) ELSE 0 END)) / NULLIF(SUM(CASE WHEN transaction_type = 'BUY' THEN quantity ELSE 0 END), 0) as avg_cost,
        (SELECT currency FROM transactions WHERE ticker = ticker_symbol AND transaction_type = 'BUY' ORDER BY transaction_date LIMIT 1) as primary_currency,
        -- Calculate total cost basis
        SUM(CASE WHEN transaction_type = 'BUY' THEN (quantity) * cost_per_share + COALESCE(commission, 0) ELSE -(quantity) * cost_per_share - COALESCE(commission, 0) END) as total_cost_basis,
        SUM(COALESCE(commission, 0)) as total_commissions,
        MIN(CASE WHEN transaction_type = 'BUY' THEN transaction_date END) as first_purchase,
        MAX(transaction_date) as last_transaction
    INTO position_data
    FROM transactions 
    WHERE ticker = ticker_symbol;

    -- Only update if we have data and positive quantity
    IF position_data.total_quantity IS NOT NULL AND position_data.total_quantity > 0 THEN
        INSERT INTO positions (
            ticker, current_quantity, avg_cost_per_share, primary_currency,
            total_cost_basis, total_commissions, first_purchase_date, last_transaction_date
        ) VALUES (
            position_data.ticker, position_data.total_quantity, position_data.avg_cost, 
            position_data.primary_currency, position_data.total_cost_basis, 
            position_data.total_commissions, position_data.first_purchase, position_data.last_transaction
        )
        ON CONFLICT (ticker) DO UPDATE SET
            current_quantity = EXCLUDED.current_quantity,
            avg_cost_per_share = EXCLUDED.avg_cost_per_share,
            primary_currency = EXCLUDED.primary_currency,
            total_cost_basis = EXCLUDED.total_cost_basis,
            total_commissions = EXCLUDED.total_commissions,
            first_purchase_date = EXCLUDED.first_purchase_date,
            last_transaction_date = EXCLUDED.last_transaction_date,
            -- Adjust current_price with fractional multiplier if fractional transactions exist
            current_price = CASE 
                WHEN positions.current_price IS NOT NULL AND positions.current_price > 0 
                THEN positions.current_price * 
                    COALESCE((SELECT fractional_multiplier FROM transactions 
                             WHERE ticker = ticker_symbol AND is_fractional = true 
                             ORDER BY transaction_date DESC LIMIT 1), 1.0)
                ELSE positions.current_price 
            END,
            -- Recalculate market values using existing current_price with fractional multiplier
            current_market_value = CASE 
                WHEN positions.current_price IS NOT NULL AND positions.current_price > 0 
                THEN EXCLUDED.current_quantity * positions.current_price * 
                    COALESCE((SELECT fractional_multiplier FROM transactions 
                             WHERE ticker = ticker_symbol AND is_fractional = true 
                             ORDER BY transaction_date DESC LIMIT 1), 1.0)
                ELSE positions.current_market_value 
            END,
            unrealized_gain_loss = CASE 
                WHEN positions.current_price IS NOT NULL AND positions.current_price > 0 
                THEN (EXCLUDED.current_quantity * positions.current_price * 
                     COALESCE((SELECT fractional_multiplier FROM transactions 
                              WHERE ticker = ticker_symbol AND is_fractional = true 
                              ORDER BY transaction_date DESC LIMIT 1), 1.0)) - EXCLUDED.total_cost_basis
                ELSE positions.unrealized_gain_loss 
            END,
            updated_at = CURRENT_TIMESTAMP;
    ELSE
        -- Delete position if no remaining quantity
        DELETE FROM positions WHERE ticker = ticker_symbol;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically recalculate positions when transactions change
CREATE OR REPLACE FUNCTION trigger_recalculate_position()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        PERFORM recalculate_position(OLD.ticker);
        RETURN OLD;
    ELSIF TG_OP = 'UPDATE' THEN
        -- For updates, recalculate both old and new ticker if they're different
        PERFORM recalculate_position(NEW.ticker);
        IF OLD.ticker != NEW.ticker THEN
            PERFORM recalculate_position(OLD.ticker);
        END IF;
        RETURN NEW;
    ELSE -- INSERT
        PERFORM recalculate_position(NEW.ticker);
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER transaction_position_update 
    AFTER INSERT OR UPDATE OR DELETE ON transactions
    FOR EACH ROW EXECUTE FUNCTION trigger_recalculate_position(); 