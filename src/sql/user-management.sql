-- ========================================
-- user-management.sql
-- ========================================

-- Drop table if exists (clean setup)
DROP TABLE IF EXISTS User;

-- Create User table
CREATE TABLE `users` (
                        `id` INT AUTO_INCREMENT PRIMARY KEY,
                        `first_name` VARCHAR(50) NOT NULL,
                        `last_name` VARCHAR(50) NOT NULL,
                        `email` VARCHAR(100) NOT NULL UNIQUE,
                        `password` VARCHAR(255),
                        `role` ENUM('ADMIN','TEACHER','STUDENT') NOT NULL,
                        `status` ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                        `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)

-- Add an index on role for faster queries
CREATE INDEX idx_role ON `users` (`role`);

-- Insert some sample users
INSERT INTO 'users' (first_name, last_name, email, password, role)
VALUES
    ('System', 'Admin', 'admin@example.com', 'admin123', 'ADMIN'),
    ('John', 'Smith', 'teacher1@example.com', 'teach123', 'TEACHER'),
    ('Jane', 'Doe', 'teacher2@example.com', 'teach123', 'TEACHER'),
    ('Alice', 'Brown', 'student1@example.com', 'stud123', 'STUDENT'),
    ('Bob', 'Green', 'student2@example.com', 'stud123', 'STUDENT');
