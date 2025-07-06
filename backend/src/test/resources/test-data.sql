-- Test data for testing
INSERT INTO users (id, username, password_hash) VALUES
(1, 'testuser1', 'hashedpassword1'),
(2, 'testuser2', 'hashedpassword2');

INSERT INTO accounts (id, account_number, balance, user_id) VALUES
(1, 'ACC12345', 1000, 1),
(2, 'ACC67890', 2000, 1),
(3, 'ACC11111', 1500, 2),
(4, 'ACC22222', 500, 2);

-- Reset sequence numbers for consistent testing (H2 compatible)
ALTER SEQUENCE SYSTEM_SEQUENCE_USERS_ID RESTART WITH 3;
ALTER SEQUENCE SYSTEM_SEQUENCE_ACCOUNTS_ID RESTART WITH 5; 