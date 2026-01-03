<?php
require_once 'config.php';

try {
    $pdo = getDbConnection();
    
    // Create messages table
    $sql = "CREATE TABLE IF NOT EXISTS messages (
        id INT AUTO_INCREMENT PRIMARY KEY,
        sender_id INT NOT NULL,
        receiver_id INT NOT NULL,
        message_text TEXT,
        media_base64 LONGTEXT,
        media_type VARCHAR(20),
        timestamp BIGINT NOT NULL,
        is_read BOOLEAN DEFAULT FALSE,
        is_edited BOOLEAN DEFAULT FALSE,
        is_deleted BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_sender (sender_id),
        INDEX idx_receiver (receiver_id),
        INDEX idx_timestamp (timestamp),
        INDEX idx_conversation (sender_id, receiver_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    
    $pdo->exec($sql);
    echo json_encode(['success' => true, 'message' => 'Messages table created successfully']);
    
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Error creating table: ' . $e->getMessage()]);
}
?>
