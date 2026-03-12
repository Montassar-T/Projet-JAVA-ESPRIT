-- =========================================================
--   TABLE: notification
-- =========================================================

CREATE TABLE notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('SYSTEM','COURSE','EVALUATION','CHAT','MARK') NOT NULL DEFAULT 'SYSTEM',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Index for fast per-user queries
CREATE INDEX idx_notification_user ON notification (user_id, is_read, created_at DESC);


-- =========================================================
-- INSERT SAMPLE DATA
-- =========================================================

INSERT INTO notification (user_id, title, message, type, is_read, read_at) VALUES
(4, 'New course available',
 'Java Fundamentals is now available in your dashboard.',
 'COURSE', FALSE, NULL),

(5, 'Quiz published',
 'Your teacher published a quiz for Web Development Basics.',
 'EVALUATION', TRUE, '2025-01-20 12:15:00');


-- =========================================================
-- TRIGGERS
-- =========================================================

-- ---------------------------------------------------------
-- 1. Evaluation published → notify all students
-- ---------------------------------------------------------
DELIMITER $$

CREATE TRIGGER trg_evaluation_published
AFTER UPDATE ON evaluations
FOR EACH ROW
BEGIN
    IF NEW.status = 'PUBLISHED' AND (OLD.status != 'PUBLISHED') THEN
        INSERT INTO notification (user_id, title, message, type)
        SELECT u.id,
               CONCAT('New ', LOWER(NEW.type), ' published'),
               CONCAT(NEW.title, ' is now available. Due: ',
                      IFNULL(DATE_FORMAT(NEW.due_date, '%d/%m/%Y %H:%i'), 'No deadline')),
               'EVALUATION'
        FROM users u
        WHERE u.role = 'STUDENT';
    END IF;
END$$

DELIMITER ;


-- ---------------------------------------------------------
-- 2. Mark inserted → notify the student
-- ---------------------------------------------------------
DELIMITER $$

CREATE TRIGGER trg_mark_inserted
AFTER INSERT ON marks
FOR EACH ROW
BEGIN
    INSERT INTO notification (user_id, title, message, type)
    SELECT NEW.student_id,
           CONCAT('New mark: ', NEW.mark, '/20'),
           CONCAT('You received ', NEW.mark, '/20 for "',
                  e.title, '"'),
           'MARK'
    FROM evaluations e
    WHERE e.id = NEW.exam_id;
END$$

DELIMITER ;


-- ---------------------------------------------------------
-- 3. Mark updated → notify the student
-- ---------------------------------------------------------
DELIMITER $$

CREATE TRIGGER trg_mark_updated
AFTER UPDATE ON marks
FOR EACH ROW
BEGIN
    IF NEW.mark != OLD.mark THEN
        INSERT INTO notification (user_id, title, message, type)
        SELECT NEW.student_id,
               CONCAT('Mark updated: ', NEW.mark, '/20'),
               CONCAT('Your mark for "', e.title,
                      '" changed from ', OLD.mark, ' to ', NEW.mark),
               'MARK'
        FROM evaluations e
        WHERE e.id = NEW.exam_id;
    END IF;
END$$

DELIMITER ;


-- ---------------------------------------------------------
-- 4. Chat message sent → notify the recipient
-- ---------------------------------------------------------
DELIMITER $$

CREATE TRIGGER trg_message_sent
AFTER INSERT ON message
FOR EACH ROW
BEGIN
    -- Determine the recipient: the user in the conversation who is NOT the sender
    INSERT INTO notification (user_id, title, message, type)
    SELECT
        CASE
            WHEN c.user1_id = NEW.sender_id THEN c.user2_id
            ELSE c.user1_id
        END,
        CONCAT('Message from ',
               (SELECT CONCAT(u.first_name, ' ', u.last_name) FROM users u WHERE u.id = NEW.sender_id)),
        LEFT(NEW.content, 100),
        'CHAT'
    FROM conversation c
    WHERE c.id = NEW.conversation_id;
END$$

DELIMITER ;


-- ---------------------------------------------------------
-- 5. New course created → notify all students
-- ---------------------------------------------------------
DELIMITER $$

CREATE TRIGGER trg_course_created
AFTER INSERT ON course
FOR EACH ROW
BEGIN
    INSERT INTO notification (user_id, title, message, type)
    SELECT u.id,
           CONCAT('New course: ', NEW.title),
           IFNULL(LEFT(NEW.description, 120), 'A new course has been added.'),
           'COURSE'
    FROM users u
    WHERE u.role = 'STUDENT';
END$$

DELIMITER ;


-- ---------------------------------------------------------
-- 6. User status changed → notify the affected user
-- ---------------------------------------------------------
DELIMITER $$

CREATE TRIGGER trg_user_status_changed
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF NEW.status != OLD.status THEN
        INSERT INTO notification (user_id, title, message, type)
        VALUES (
            NEW.id,
            CONCAT('Account ', LOWER(NEW.status)),
            CONCAT('Your account status has been changed to ', LOWER(NEW.status), '.'),
            'SYSTEM'
        );
    END IF;
END$$

DELIMITER ;
