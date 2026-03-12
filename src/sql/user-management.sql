-- ========================================
-- user-management.sql
-- Complete schema initialization for users and classes
-- Use this for fresh database setup (DROPS all data)
-- Use student-classes.sql for safe migration on existing databases
-- ========================================

-- Disable foreign key checks to allow dropping tables with dependencies
SET FOREIGN_KEY_CHECKS = 0;

-- ========== DROP EXISTING TABLES ==========
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS school_class;

-- ========== CREATE SCHOOL CLASS TABLE ==========
-- Stores student class/group information
-- Example: 1ère Informatique A, 2ème Informatique B, etc.
CREATE TABLE `school_class` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT 'Class name (e.g., 1ère Informatique A)',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique class code (e.g., INFO1-A)',
    `level` VARCHAR(50) COMMENT 'Academic level (e.g., 1ère année, 2ème année)',
    `capacity` INT COMMENT 'Maximum students in this class',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== CREATE USERS TABLE ==========
-- Main user table with support for multiple roles
-- ADMIN / TEACHER / STUDENT
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `first_name` VARCHAR(50) NOT NULL,
    `last_name` VARCHAR(50) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) COMMENT 'Hashed password (bcrypt)',
    `role` ENUM('ADMIN','TEACHER','STUDENT') NOT NULL COMMENT 'User role',
    `school_class_id` BIGINT NULL COMMENT 'Reference to school_class (only for STUDENT role)',
    `status` ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key to school_class (students only)
    CONSTRAINT `fk_users_school_class`
        FOREIGN KEY (`school_class_id`) REFERENCES `school_class`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== CREATE INDEXES ==========
CREATE INDEX `idx_role` ON `users` (`role`);
CREATE INDEX `idx_users_school_class` ON `users` (`school_class_id`);
CREATE INDEX `idx_email` ON `users` (`email`);

-- ========== INSERT SAMPLE DATA ==========

-- Sample classes
INSERT INTO `school_class` (`name`, `code`, `level`, `capacity`)
VALUES
    ('1ère Informatique A', 'INFO1-A', '1ère année', 30),
    ('1ère Informatique B', 'INFO1-B', '1ère année', 30),
    ('2ème Informatique A', 'INFO2-A', '2ème année', 28);

-- Sample users (password: all are 'password123' hashed with bcrypt)
INSERT INTO `users` (first_name, last_name, email, password, role, school_class_id, status)
VALUES
    ('System', 'Admin', 'admin@educlass.tn', '$2a$10$3F.ouSzpA7tMnlnNV/jtcehO8wLfUg3Iw9wghWoskaHRW3zL9Tha2', 'ADMIN', NULL, 'ACTIVE'),
    ('John', 'Smith', 'teacher1@educlass.tn', '$2a$10$3F.ouSzpA7tMnlnNV/jtcehO8wLfUg3Iw9wghWoskaHRW3zL9Tha2', 'TEACHER', NULL, 'ACTIVE'),
    ('Jane', 'Doe', 'teacher2@educlass.tn', '$2a$10$3F.ouSzpA7tMnlnNV/jtcehO8wLfUg3Iw9wghWoskaHRW3zL9Tha2', 'TEACHER', NULL, 'ACTIVE'),
    ('Alice', 'Brown', 'student1@educlass.tn', '$2a$10$3F.ouSzpA7tMnlnNV/jtcehO8wLfUg3Iw9wghWoskaHRW3zL9Tha2', 'STUDENT', 1, 'ACTIVE'),
    ('Bob', 'Green', 'student2@educlass.tn', '$2a$10$3F.ouSzpA7tMnlnNV/jtcehO8wLfUg3Iw9wghWoskaHRW3zL9Tha2', 'STUDENT', 2, 'ACTIVE');

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ========== NOTES ==========
-- 1) For ADMIN/TEACHER users: school_class_id should be NULL
-- 2) For STUDENT users: school_class_id should reference a valid school_class.id
-- 3) Sample password is 'password123' (bcrypt hashed)
-- 4) Default status is 'ACTIVE' for new users

