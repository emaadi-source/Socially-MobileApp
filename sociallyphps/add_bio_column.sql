-- Add bio column to sociallypersonal database
-- Run this in phpMyAdmin

USE sociallypersonal;

ALTER TABLE users ADD COLUMN bio TEXT DEFAULT '';

-- Verify the column was added
DESCRIBE users;
