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
INSERT INTO `users` (first_name, last_name, email, password, role)
VALUES
    ('System', 'Admin', 'admin@educlass.tn', '$2a$10$3F.ouSzpA7tMnlnNV/jtcehO8wLfUg3Iw9wghWoskaHRW3zL9Tha2', 'ADMIN'),
    ('John', 'Smith', 'teacher1@educlass.tn', '$2a$10$3F.ouSzpA7tMnlnNV/jtcehO8wLfUg3Iw9wghWoskaHRW3zL9Tha2', 'TEACHER'),
    ('Jane', 'Doe', 'teacher2@educlass.tn', '$2a$10$3F.ouSzpA7tMnlnNV/jtcehO8wLfUg3Iw9wghWoskaHRW3zL9Tha2', 'TEACHER'),
    ('Alice', 'Brown', 'student1@educlass.tn', '$2a$10$3F.ouSzpA7tMnlnNV/jtcehO8wLfUg3Iw9wghWoskaHRW3zL9Tha2', 'STUDENT'),
    ('Bob', 'Green', 'student2@educlass.tn', '$2a$10$3F.ouSzpA7tMnlnNV/jtcehO8wLfUg3Iw9wghWoskaHRW3zL9Tha2', 'STUDENT');
