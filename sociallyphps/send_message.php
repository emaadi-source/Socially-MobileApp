<?php
require_once 'config.php';

// Get JSON input
$json = file_get_contents('php://input');
$data = json_decode($json, true);

// Try both POST and JSON input
$senderId = $data['senderId'] ?? $_POST['senderId'] ?? '';
$receiverId = $data['receiverId'] ?? $_POST['receiverId'] ?? '';
$text = $data['text'] ?? $_POST['text'] ?? '';
$mediaBase64 = $data['mediaBase64'] ?? $_POST['mediaBase64'] ?? '';
$mediaType = $data['mediaType'] ?? $_POST['mediaType'] ?? '';

if (empty($senderId) || empty($receiverId)) {
    echo json_encode(['success' => false, 'message' => 'Sender and Receiver IDs required']);
    exit;
}

try {
    $pdo = getDbConnection();
    
    // Generate unique message ID
    $messageId = uniqid('msg_', true);
    $timestamp = round(microtime(true) * 1000);
    
    // FIXED: use 'text' column name
    $sql = "INSERT INTO messages (message_id, sender_id, receiver_id, text, media_base64, media_type, timestamp) 
            VALUES (?, ?, ?, ?, ?, ?, ?)";
    $stmt = $pdo->prepare($sql);
    
    if ($stmt->execute([$messageId, $senderId, $receiverId, $text, $mediaBase64, $mediaType, $timestamp])) {
        echo json_encode([
            'success' => true, 
            'messageId' => $pdo->lastInsertId(), 
            'timestamp' => $timestamp
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to execute insert']);
    }
} catch (PDOException $e) {
    error_log("Database error in send_message.php: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
