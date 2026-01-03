-- ============================================
-- Database Migration: Add Notifications and Update Tables
-- Run this in phpMyAdmin for sociallypersonal database
-- ============================================

USE sociallypersonal;

-- Add status column to followers table for follow requests
ALTER TABLE followers 
ADD COLUMN status ENUM('pending', 'accepted') DEFAULT 'accepted' AFTER follower_id;

-- Add status column to following table for consistency
ALTER TABLE following 
ADD COLUMN status ENUM('pending', 'accepted') DEFAULT 'accepted' AFTER following_id;

-- Add is_logged_in and last_active to sessions table
ALTER TABLE sessions 
ADD COLUMN is_logged_in BOOLEAN DEFAULT TRUE AFTER is_active,
ADD COLUMN last_active BIGINT AFTER updated_at;

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT 'Receiver of notification',
    sender_id INT NOT NULL COMMENT 'Person who triggered notification',
    type ENUM('follow_request', 'follow_accept', 'follow_back', 'like', 'comment') NOT NULL,
    post_id INT NULL COMMENT 'Related post (for likes/comments)',
    is_read BOOLEAN DEFAULT FALSE,
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add bio column to users if not exists
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS bio TEXT AFTER last_name;
