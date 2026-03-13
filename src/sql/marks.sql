-- ========================================
-- marks.sql
-- Marks management module for student evaluation marks
-- Marks are auto-calculated for SINGLE_CHOICE and MULTIPLE_CHOICE questions
-- ========================================

-- Drop table if exists (clean setup)
DROP TABLE IF EXISTS marks;

-- Create marks table
-- Run after user-management.sql and evaluations.sql (depends on users and evaluations)
CREATE TABLE marks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    exam_id INT NOT NULL,
    mark DECIMAL(5,2) NOT NULL CHECK (mark >= 0 AND mark <= 20),
    -- review_requested: student has requested a double correction
    review_requested TINYINT(1) NOT NULL DEFAULT 0,
    -- review_resolved: teacher has checked/reviewed the request
    review_resolved TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- Unique constraint: one student can have only one mark per exam
    CONSTRAINT uk_student_exam UNIQUE (student_id, exam_id),
    -- Foreign key: mark belongs to one student (users.id, typically role STUDENT)
    CONSTRAINT fk_marks_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    -- Foreign key: mark belongs to one exam (evaluations.id, type EXAM)
    CONSTRAINT fk_marks_exam FOREIGN KEY (exam_id) REFERENCES evaluations(id) ON DELETE CASCADE
);

-- Add index on student_id for faster queries when retrieving all marks for a student
CREATE INDEX idx_student_id ON marks (student_id);

-- Add index on exam_id for faster queries when retrieving all marks for an exam
CREATE INDEX idx_exam_id ON marks (exam_id);

-- Insert sample data
-- Note: Adjust student_id and exam_id values based on your actual data
INSERT INTO marks (student_id, exam_id, mark)
VALUES
    (4, 1, 15.50),  -- Student 4, Exam 1: 15.50/20
    (4, 2, 18.00),  -- Student 4, Exam 2: 18.00/20
    (5, 1, 12.75),  -- Student 5, Exam 1: 12.75/20
    (5, 2, 16.25),  -- Student 5, Exam 2: 16.25/20
    (4, 3, 19.50),  -- Student 4, Exam 3: 19.50/20
    (5, 3, 14.00);  -- Student 5, Exam 3: 14.00/20
