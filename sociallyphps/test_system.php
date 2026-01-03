<?php
require_once 'config.php';

echo "<h1>Complete System Test</h1>";

try {
    $pdo = getDbConnection();
    echo "<p style='color: green;'>✓ Database connection successful</p>";
    
    // Test 1: Check tables exist
    echo "<h2>1. Tables Check</h2>";
    $tables = ['users', 'sessions', 'messages', 'calls'];
    foreach ($tables as $table) {
        $stmt = $pdo->query("SHOW TABLES LIKE '$table'");
        if ($stmt->rowCount() > 0) {
            echo "✓ Table '$table' exists<br>";
        } else {
            echo "<span style='color: red;'>✗ Table '$table' MISSING</span><br>";
        }
    }
    
    // Test 2: Check messages table structure
    echo "<h2>2. Messages Table Structure</h2>";
    $stmt = $pdo->query("DESCRIBE messages");
    $columns = $stmt->fetchAll(PDO::FETCH_COLUMN);
    $requiredColumns = ['id', 'message_id', 'sender_id', 'receiver_id', 'text', 'media_base64', 'media_type', 'timestamp'];
    foreach ($requiredColumns as $col) {
        if (in_array($col, $columns)) {
            echo "✓ Column '$col' exists<br>";
        } else {
            echo "<span style='color: red;'>✗ Column '$col' MISSING</span><br>";
        }
    }
    
    // Test 3: Count messages
    echo "<h2>3. Messages in Database</h2>";
    $stmt = $pdo->query("SELECT COUNT(*) as count FROM messages");
    $count = $stmt->fetch()['count'];
    echo "Total messages: <strong>$count</strong><br>";
    
    if ($count > 0) {
        echo "<h3>Latest 5 messages:</h3>";
        $stmt = $pdo->query("SELECT id, sender_id, receiver_id, text, timestamp FROM messages ORDER BY id DESC LIMIT 5");
        $messages = $stmt->fetchAll(PDO::FETCH_ASSOC);
        echo "<table border='1'>";
        echo "<tr><th>ID</th><th>From</th><th>To</th><th>Text</th><th>Timestamp</th></tr>";
        foreach ($messages as $msg) {
            echo "<tr>";
            echo "<td>" . $msg['id'] . "</td>";
            echo "<td>" . $msg['sender_id'] . "</td>";
            echo "<td>" . $msg['receiver_id'] . "</td>";
            echo "<td>" . htmlspecialchars($msg['text']) . "</td>";
            echo "<td>" . date('Y-m-d H:i:s', $msg['timestamp'] / 1000) . "</td>";
            echo "</tr>";
        }
        echo "</table>";
    }
    
    // Test 4: Test API endpoints
    echo "<h2>4. API Endpoints Test</h2>";
    $endpoints = [
        'send_message.php',
        'get_messages.php',
        'initiate_call.php',
        'get_user_status.php'
    ];
    
    foreach ($endpoints as $endpoint) {
        if (file_exists($endpoint)) {
            echo "✓ $endpoint exists<br>";
        } else {
            echo "<span style='color: red;'>✗ $endpoint MISSING</span><br>";
        }
    }
    
    echo "<h2 style='color: green;'>✓ System Check Complete</h2>";
    echo "<p><strong>Next steps:</strong></p>";
    echo "<ol>";
    echo "<li>Rebuild your Android app</li>";
    echo "<li>Open chat and send a message</li>";
    echo "<li>Messages should appear on right (yours) and left (theirs)</li>";
    echo "<li>Try initiating a call</li>";
    echo "</ol>";
    
} catch (PDOException $e) {
    echo "<p style='color: red;'>✗ Database error: " . $e->getMessage() . "</p>";
}
?>
