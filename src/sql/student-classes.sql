-- ========================================
-- student-classes.sql
-- Migration for student class support
-- Safe update (no DROP, preserves existing data)
-- ========================================

-- Disable foreign key checks during migration
SET FOREIGN_KEY_CHECKS = 0;

-- 1) Create classes table if it doesn't exist
CREATE TABLE IF NOT EXISTS `school_class` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `level` VARCHAR(50),
    `capacity` INT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2) Add nullable class reference to users (if not already present)
ALTER TABLE `users`
    ADD COLUMN IF NOT EXISTS `school_class_id` BIGINT NULL;

-- 3) Add index for faster filters / joins (if not already present)
ALTER TABLE `users`
    ADD INDEX IF NOT EXISTS `idx_users_school_class` (`school_class_id`);

-- 4) Add foreign key (if not already present)
-- First drop the existing constraint if it exists
ALTER TABLE `users`
    DROP FOREIGN KEY IF EXISTS `fk_users_school_class`;

-- Then add it back
ALTER TABLE `users`
    ADD CONSTRAINT `fk_users_school_class`
    FOREIGN KEY (`school_class_id`) REFERENCES `school_class`(`id`) ON DELETE SET NULL;

-- 5) Seed sample classes (only if table is empty)
INSERT IGNORE INTO `school_class` (`name`, `code`, `level`, `capacity`)
VALUES
    ('1ère Informatique A', 'INFO1-A', '1ère année', 30),
    ('1ère Informatique B', 'INFO1-B', '1ère année', 30),
    ('2ème Informatique A', 'INFO2-A', '2ème année', 28);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Optional: Assign existing students to classes (uncomment to use)
-- UPDATE `users` SET `school_class_id` = 1 WHERE `role` = 'STUDENT' AND `email` LIKE '%student1%' AND `school_class_id` IS NULL;
-- UPDATE `users` SET `school_class_id` = 2 WHERE `role` = 'STUDENT' AND `email` LIKE '%student2%' AND `school_class_id` IS NULL;

