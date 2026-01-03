-- ============================================
-- Social Media App Database Schema
-- Database: sociallypersonal
-- Created for XAMPP MySQL
-- ============================================

-- Create database
CREATE DATABASE IF NOT EXISTS sociallypersonal;
USE sociallypersonal;

-- ============================================
-- Table: users
-- Stores user account information and profiles
-- ============================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    profile_pic_base64 LONGTEXT,
    device_id VARCHAR(255),
    last_login BIGINT,
    is_logged_in BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_is_logged_in (is_logged_in)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: posts
-- Stores user posts with media content
-- ============================================
CREATE TABLE posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    post_id VARCHAR(100) NOT NULL UNIQUE,
    media_base64 LONGTEXT,
    media_type VARCHAR(20) NOT NULL,
    caption TEXT,
    timestamp BIGINT NOT NULL,
    likes INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: stories
-- Stores temporary stories (24-hour lifespan)
-- ============================================
CREATE TABLE stories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    story_id VARCHAR(100) NOT NULL UNIQUE,
    media_base64 LONGTEXT,
    media_type VARCHAR(20) NOT NULL,
    timestamp BIGINT NOT NULL,
    chunk_count INT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_story_id (story_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: story_chunks
-- Stores story media data in chunks
-- ============================================
CREATE TABLE story_chunks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    story_id INT NOT NULL,
    chunk_index INT NOT NULL,
    chunk_data LONGTEXT NOT NULL,
    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    INDEX idx_story_id (story_id),
    UNIQUE KEY unique_story_chunk (story_id, chunk_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: comments
-- Stores comments on posts
-- ============================================
CREATE TABLE comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    comment_id VARCHAR(100) NOT NULL UNIQUE,
    text TEXT NOT NULL,
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_comment_id (comment_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: likes
-- Stores post likes (one like per user per post)
-- ============================================
CREATE TABLE likes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_like (post_id, user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: followers
-- Stores follower relationships
-- ============================================
CREATE TABLE followers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT 'The user being followed',
    follower_id INT NOT NULL COMMENT 'The follower',
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_follower (user_id, follower_id),
    INDEX idx_user_id (user_id),
    INDEX idx_follower_id (follower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: following
-- Stores following relationships (mirror of followers)
-- ============================================
CREATE TABLE following (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT 'The user doing the following',
    following_id INT NOT NULL COMMENT 'The user being followed',
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_following (user_id, following_id),
    INDEX idx_user_id (user_id),
    INDEX idx_following_id (following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: sessions
-- Stores active user sessions
-- ============================================
CREATE TABLE sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    device_id VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: messages
-- Stores direct messages between users
-- ============================================
CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message_id VARCHAR(100) NOT NULL UNIQUE,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    text TEXT NOT NULL,
    timestamp BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_message_id (message_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_conversation (sender_id, receiver_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: user_tokens
-- Stores FCM tokens for push notifications
-- ============================================
CREATE TABLE user_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    fcm_token VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_token (user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Insert sample data (optional - for testing)
-- ============================================

-- Sample user (password: "password123")
-- Password hash generated using PHP password_hash()
INSERT INTO users (email, username, password_hash, first_name, last_name, dob, device_id, last_login, is_logged_in) 
VALUES (
    'test@example.com',
    'testuser',
    '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test',
    'User',
    '2000-01-01',
    'test_device_123',
    UNIX_TIMESTAMP() * 1000,
    FALSE
);

-- ============================================
-- Cleanup procedures (optional)
-- ============================================

-- Procedure to delete stories older than 24 hours
DELIMITER //
CREATE PROCEDURE cleanup_old_stories()
BEGIN
    DECLARE cutoff_time BIGINT;
    SET cutoff_time = (UNIX_TIMESTAMP() - 86400) * 1000; -- 24 hours ago in milliseconds
    
    DELETE FROM stories WHERE timestamp < cutoff_time;
END //
DELIMITER ;

-- Event to automatically run cleanup daily (requires event scheduler to be enabled)
-- To enable: SET GLOBAL event_scheduler = ON;
CREATE EVENT IF NOT EXISTS daily_story_cleanup
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO CALL cleanup_old_stories();

-- ============================================
-- Useful queries for maintenance
-- ============================================

-- Count users
-- SELECT COUNT(*) as total_users FROM users;

-- Count posts per user
-- SELECT u.username, COUNT(p.id) as post_count 
-- FROM users u 
-- LEFT JOIN posts p ON u.id = p.user_id 
-- GROUP BY u.id;

-- Get active sessions
-- SELECT u.username, s.session_id, s.device_id, FROM_UNIXTIME(s.created_at/1000) as created 
-- FROM sessions s 
-- JOIN users u ON s.user_id = u.id 
-- WHERE s.is_active = TRUE;

-- Get recent posts with user info
-- SELECT p.post_id, u.username, p.caption, FROM_UNIXTIME(p.timestamp/1000) as posted_at, p.likes, p.comments_count
-- FROM posts p
-- JOIN users u ON p.user_id = u.id
-- ORDER BY p.timestamp DESC
-- LIMIT 20;

-- ============================================
-- End of schema
-- ============================================
