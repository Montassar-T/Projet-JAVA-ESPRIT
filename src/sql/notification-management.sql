-- =========================================================
--   TABLE: notification
-- =========================================================

CREATE TABLE notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('SYSTEM','COURSE','EVALUATION') NOT NULL DEFAULT 'SYSTEM',
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
