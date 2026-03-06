-- ========================================
-- student-responses.sql
-- Student responses for evaluations
-- Stores which choices a student selected and open answers
-- ========================================

-- Drop table if exists (clean setup)
DROP TABLE IF EXISTS student_responses;

-- Create student_responses table
-- Run after user-management.sql and evaluations.sql
CREATE TABLE student_responses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    question_id INT NOT NULL,
    choice_id INT NULL,
    answer_text TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- Foreign keys
    CONSTRAINT fk_response_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_response_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_response_choice FOREIGN KEY (choice_id) REFERENCES choices(id) ON DELETE CASCADE
);

-- Index for fast lookups
CREATE INDEX idx_response_student ON student_responses (student_id);
CREATE INDEX idx_response_question ON student_responses (question_id);