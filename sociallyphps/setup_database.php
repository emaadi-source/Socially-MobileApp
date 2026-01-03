<?php
require_once 'config.php';

try {
    $pdo = getDbConnection();
    
    echo "<h2>Database Setup - Complete Fix</h2>";
    
    // 1. Create sessions table
    echo "<h3>1. Creating sessions table...</h3>";
    try {
        $sql = "CREATE TABLE IF NOT EXISTS sessions (
            id INT AUTO_INCREMENT PRIMARY KEY,
            user_id INT NOT NULL,
            session_id VARCHAR(100) NOT NULL UNIQUE,
            device_id VARCHAR(255),
            is_active BOOLEAN DEFAULT TRUE,
            created_at BIGINT NOT NULL,
            updated_at BIGINT NOT NULL,
            INDEX idx_user_id (user_id),
            INDEX idx_session_id (session_id),
            INDEX idx_is_active (is_active)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        $pdo->exec($sql);
        echo "✓ Sessions table created/verified<br>";
    } catch (PDOException $e) {
        echo "✗ Error: " . $e->getMessage() . "<br>";
    }
    
    // 2. Create messages table (matching schema)
    echo "<h3>2. Creating messages table...</h3>";
    try {
        // Drop old messages table if it exists with wrong schema
        $pdo->exec("DROP TABLE IF EXISTS messages");
        
        $sql = "CREATE TABLE messages (
            id INT AUTO_INCREMENT PRIMARY KEY,
            message_id VARCHAR(100) NOT NULL UNIQUE,
            sender_id INT NOT NULL,
            receiver_id INT NOT NULL,
            text TEXT,
            media_base64 LONGTEXT,
            media_type VARCHAR(20),
            timestamp BIGINT NOT NULL,
            is_read BOOLEAN DEFAULT FALSE,
            is_edited BOOLEAN DEFAULT FALSE,
            is_deleted BOOLEAN DEFAULT FALSE,
            INDEX idx_message_id (message_id),
            INDEX idx_sender_id (sender_id),
            INDEX idx_receiver_id (receiver_id),
            INDEX idx_timestamp (timestamp),
            INDEX idx_conversation (sender_id, receiver_id, timestamp)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        $pdo->exec($sql);
        echo "✓ Messages table created<br>";
    } catch (PDOException $e) {
        echo "✗ Error: " . $e->getMessage() . "<br>";
    }
    
    // 3. Create calls table
    echo "<h3>3. Creating calls table...</h3>";
    try {
        $sql = "CREATE TABLE IF NOT EXISTS calls (
            id INT AUTO_INCREMENT PRIMARY KEY,
            caller_id INT NOT NULL,
            receiver_id INT NOT NULL,
            channel_name VARCHAR(255) NOT NULL,
            type VARCHAR(10) NOT NULL,
            status VARCHAR(20) DEFAULT 'pending',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        $pdo->exec($sql);
        echo "✓ Calls table created/verified<br>";
    } catch (PDOException $e) {
        echo "✗ Error: " . $e->getMessage() . "<br>";
    }
    
    // 4. Verify all tables
    echo "<h3>4. Verifying tables...</h3>";
    $tables = ['users', 'sessions', 'messages', 'calls'];
    $allGood = true;
    foreach ($tables as $table) {
        $stmt = $pdo->query("SHOW TABLES LIKE '$table'");
        if ($stmt->rowCount() > 0) {
            echo "✓ Table '$table' exists<br>";
        } else {
            echo "✗ Table '$table' NOT FOUND<br>";
            $allGood = false;
        }
    }
    
    if ($allGood) {
        echo "<br><h2 style='color: green;'>✓ ALL TABLES READY!</h2>";
        echo "<p><strong>You can now:</strong></p>";
        echo "<ol>";
        echo "<li>Rebuild your Android app</li>";
        echo "<li>Send messages (they will save to database)</li>";
        echo "<li>See online status (from sessions)</li>";
        echo "<li>Make voice/video calls</li>";
        echo "</ol>";
    } else {
        echo "<br><h2 style='color: red;'>✗ Some tables are missing!</h2>";
    }
    
} catch (PDOException $e) {
    echo "<h2 style='color: red;'>✗ Database Connection Error</h2>";
    echo "<p>" . $e->getMessage() . "</p>";
}
?>
