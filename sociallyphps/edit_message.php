<?php
require_once 'config.php';

$messageId = $_POST['messageId'] ?? '';
$newText = $_POST['newText'] ?? '';

if (empty($messageId) || empty($newText)) {
    echo json_encode(['success' => false, 'message' => 'Message ID and new text required']);
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
        echo json_encode(['success' => false, 'message' => 'You can only edit messages within 5 minutes of sending']);
        exit;
    }

    $sql = "UPDATE messages SET message_text = ?, is_edited = 1 WHERE id = ?";
    $stmt = $pdo->prepare($sql);
    
    if ($stmt->execute([$newText, $messageId])) {
        echo json_encode(['success' => true, 'message' => 'Message updated']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to update message']);
    }
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
