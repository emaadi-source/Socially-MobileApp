<?php
require_once 'config.php';

$messageId = $_POST['messageId'] ?? '';

if (empty($messageId)) {
    echo json_encode(['success' => false, 'message' => 'Message ID required']);
    exit;
}

try {
    $pdo = getDbConnection();
    
    // Check timestamp first
    $checkSql = "SELECT timestamp FROM messages WHERE id = ?";
    $checkStmt = $pdo->prepare($checkSql);
    $checkStmt->execute([$messageId]);
    $message = $checkStmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$message) {
        echo json_encode(['success' => false, 'message' => 'Message not found']);
        exit;
    }
    
    $timestamp = $message['timestamp'];
    $currentTime = round(microtime(true) * 1000);
    $fiveMinutesInMillis = 5 * 60 * 1000;
    
    if (($currentTime - $timestamp) > $fiveMinutesInMillis) {
        echo json_encode(['success' => false, 'message' => 'You can only delete messages within 5 minutes of sending']);
        exit;
    }

    // Soft delete
    $sql = "UPDATE messages SET is_deleted = 1 WHERE id = ?";
    $stmt = $pdo->prepare($sql);
    
    if ($stmt->execute([$messageId])) {
        echo json_encode(['success' => true, 'message' => 'Message deleted']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to delete message']);
    }
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
