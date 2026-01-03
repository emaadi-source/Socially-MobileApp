<?php
require_once 'config.php';

$userId = $_GET['userId'] ?? '';
$otherUserId = $_GET['otherUserId'] ?? '';
$lastId = $_GET['lastId'] ?? 0;

if (empty($userId) || empty($otherUserId)) {
    echo json_encode(['success' => false, 'message' => 'User IDs required']);
    exit;
}

try {
    $pdo = getDbConnection();
    $sql = "SELECT * FROM messages 
            WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?))
            AND id > ?
            AND is_deleted = 0
            ORDER BY timestamp ASC";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([$userId, $otherUserId, $otherUserId, $userId, $lastId]);
    $results = $stmt->fetchAll();

    $messages = [];
    foreach ($results as $row) {
        $messages[] = [
            'id' => $row['id'],
            'senderId' => $row['sender_id'],
            'receiverId' => $row['receiver_id'],
            'text' => $row['text'] ?? '',  // FIXED: use 'text' column
            'mediaBase64' => $row['media_base64'] ?? '',
            'mediaType' => $row['media_type'] ?? '',
            'timestamp' => $row['timestamp'],
            'isRead' => $row['is_read'],
            'isEdited' => $row['is_edited'],
            'isDeleted' => $row['is_deleted']
        ];
    }

    echo json_encode(['success' => true, 'messages' => $messages]);

} catch (PDOException $e) {
    error_log("Database error in get_messages.php: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
