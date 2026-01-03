<?php
/**
 * Get Conversations API
 * 
 * GET /get_conversations.php?userId=xxx
 * Retrieves all conversations for a user with last message
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$params = validateGetParams(['userId']);
$userId = (int)$params['userId'];

try {
    $pdo = getDbConnection();
    
    // Get unique conversation partners with last message
    $stmt = $pdo->prepare("
        SELECT DISTINCT
            CASE 
                WHEN m.sender_id = ? THEN m.receiver_id
                ELSE m.sender_id
            END as partner_id,
            u.username, u.first_name, u.last_name, u.profile_pic_base64,
            (SELECT message_text FROM messages m2 
             WHERE (m2.sender_id = ? AND m2.receiver_id = partner_id)
                OR (m2.sender_id = partner_id AND m2.receiver_id = ?)
             ORDER BY m2.timestamp DESC LIMIT 1) as last_message,
            (SELECT timestamp FROM messages m2 
             WHERE (m2.sender_id = ? AND m2.receiver_id = partner_id)
                OR (m2.sender_id = partner_id AND m2.receiver_id = ?)
             ORDER BY m2.timestamp DESC LIMIT 1) as last_timestamp,
            (SELECT COUNT(*) FROM messages m2 
             WHERE m2.receiver_id = ? AND m2.sender_id = partner_id AND m2.is_read = ?) as unread_count
        FROM messages m
        JOIN users u ON u.id = CASE 
            WHEN m.sender_id = ? THEN m.receiver_id
            ELSE m.sender_id
        END
        WHERE m.sender_id = ? OR m.receiver_id = ?
        ORDER BY last_timestamp DESC
    ");
    
    $stmt->execute([
        $userId, $userId, $userId, $userId, $userId, $userId, false, $userId, $userId, $userId
    ]);
    
    $conversations = [];
    while ($row = $stmt->fetch()) {
        $conversations[] = [
            'partnerId' => (int)$row['partner_id'],
            'username' => $row['username'],
            'firstName' => $row['first_name'],
            'lastName' => $row['last_name'],
            'profilePicBase64' => $row['profile_pic_base64'],
            'lastMessage' => $row['last_message'],
            'lastTimestamp' => (int)$row['last_timestamp'],
            'unreadCount' => (int)$row['unread_count']
        ];
    }
    
    sendSuccess('Conversations retrieved', [
        'conversations' => $conversations,
        'count' => count($conversations)
    ]);
    
} catch (PDOException $e) {
    error_log("Get conversations error: " . $e->getMessage());
    sendError('Failed to retrieve conversations', 500);
}

?>
