-- ========================================
-- admin-management.sql
-- Non-destructive admin schema setup
-- ========================================
CREATE TABLE IF NOT EXISTS supervision (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    user VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    result VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
