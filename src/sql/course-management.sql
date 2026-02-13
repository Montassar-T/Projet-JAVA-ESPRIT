-- =========================================================
--   TABLE: course
-- =========================================================

CREATE TABLE course (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    level INT DEFAULT 1,
    creation_date DATETIME NOT NULL,

    PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- =========================================================
--   TABLE: chapter
-- =========================================================

CREATE TABLE chapter (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    course_id BIGINT NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_chapter_course
        FOREIGN KEY (course_id)
        REFERENCES course(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_chapter_order
        UNIQUE (course_id, order_index)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- =========================================================
-- TABLE: lesson
-- =========================================================

CREATE TABLE lesson (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    duration_minutes INT NOT NULL,
    chapter_id BIGINT NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_lesson_chapter
        FOREIGN KEY (chapter_id)
        REFERENCES chapter(id)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- =========================================================
-- INSERT SAMPLE DATA
-- =========================================================

INSERT INTO course (title, description, level, creation_date) VALUES
('Java Fundamentals',
 'Introduction to core Java concepts and object-oriented programming.',
 1,
 '2025-01-10 09:00:00'),

('Web Development Basics',
 'Foundations of HTML, CSS, and JavaScript for beginners.',
 2,
 '2025-01-15 10:30:00');


INSERT INTO chapter (title, order_index, course_id) VALUES
('Introduction to Java', 1, 1),
('Object-Oriented Concepts', 2, 1),
('Java Collections Framework', 3, 1),

('HTML Basics', 1, 2),
('CSS Fundamentals', 2, 2),
('JavaScript Introduction', 3, 2);


INSERT INTO lesson (title, content, duration_minutes, chapter_id) VALUES
('What is Java?',
 'Overview of Java, JVM, and platform independence.',
 30, 1),

('Setting up the Java Environment',
 'Installing JDK and configuring the development environment.',
 45, 1),

('Classes and Objects',
 'Understanding classes, objects, and encapsulation.',
 50, 2),

('Inheritance and Polymorphism',
 'Exploring inheritance, method overriding, and polymorphism.',
 60, 2),

('Lists and Sets',
 'Working with List and Set interfaces in Java.',
 40, 3),

('Maps',
 'Understanding HashMap, TreeMap, and use cases.',
 45, 3),

('HTML Structure',
 'Basic structure of an HTML document.',
 30, 4),

('Common HTML Tags',
 'Headings, paragraphs, links, and images.',
 35, 4),

('CSS Selectors',
 'Using selectors to style HTML elements.',
 40, 5),

('Box Model',
 'Understanding margin, border, padding, and content.',
 45, 5),

('JavaScript Basics',
 'Variables, data types, and basic syntax.',
 50, 6),

('DOM Manipulation',
 'Interacting with HTML elements using JavaScript.',
 55, 6);