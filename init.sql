CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    id SERIAL PRIMARY KEY,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    balance BIGINT NOT NULL DEFAULT 0,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_account_id INTEGER NOT NULL,
    to_account_id INTEGER NOT NULL,
    amount BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (to_account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_to_account ON transactions(to_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);

INSERT INTO users (username, password_hash) VALUES 
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFzqeNVcQWJiWXy6iMKwKzS'),
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFzqeNVcQWJiWXy6iMKwKzS')
ON CONFLICT (username) DO NOTHING;

INSERT INTO accounts (account_number, balance, user_id) VALUES 
('ACC001', 100000, 1),
('ACC002', 50000, 1),
('ACC003', 200000, 2)
ON CONFLICT (account_number) DO NOTHING; 