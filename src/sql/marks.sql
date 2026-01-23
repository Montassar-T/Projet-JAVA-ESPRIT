-- ========================================
-- marks.sql
-- Marks management module for student exam marks
-- ========================================

-- Drop table if exists (clean setup)
DROP TABLE IF EXISTS marks;

-- Create marks table
CREATE TABLE marks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    exam_id INT NOT NULL,
    mark DECIMAL(5,2) NOT NULL CHECK (mark >= 0 AND mark <= 20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- Unique constraint: one student can have only one mark per exam
    CONSTRAINT uk_student_exam UNIQUE (student_id, exam_id)
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
