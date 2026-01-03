<?php
/**
 * Get Notifications API
 * 
 * GET /get_notifications.php?userId=xxx
 * Retrieves all notifications for a user
 */

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$userId = isset($_GET['userId']) ? (int)$_GET['userId'] : null;

if (!$userId) {
    sendError('userId is required');
}

try {
    $pdo = getDbConnection();
    
    // Get notifications with sender info and post details
    $stmt = $pdo->prepare("
        SELECT 
            n.id,
            n.type,
            n.post_id,
            n.is_read,
            n.timestamp,
            u.id as sender_id,
            u.username as sender_username,
            u.first_name as sender_first_name,
            u.last_name as sender_last_name,
            u.profile_pic_base64 as sender_profile_pic,
            p.media_base64 as post_media,
            p.likes as post_likes,
            p.comments_count as post_comments
        FROM notifications n
        JOIN users u ON n.sender_id = u.id
        LEFT JOIN posts p ON n.post_id = p.id
        WHERE n.user_id = ?
        ORDER BY n.timestamp DESC
        LIMIT 50
    ");
    $stmt->execute([$userId]);
    $notifications = $stmt->fetchAll();
    
    $results = [];
    foreach ($notifications as $notif) {
        $notificationData = [
            'id' => (int)$notif['id'],
            'type' => $notif['type'],
            'postId' => $notif['post_id'] ? (int)$notif['post_id'] : null,
            'isRead' => (bool)$notif['is_read'],
            'timestamp' => (int)$notif['timestamp'],
            'sender' => [
                'id' => (int)$notif['sender_id'],
                'username' => $notif['sender_username'],
                'firstName' => $notif['sender_first_name'],
                'lastName' => $notif['sender_last_name'],
                'profilePicBase64' => $notif['sender_profile_pic']
            ]
        ];
        
        // Add post details for like/comment notifications
        if ($notif['post_id'] && in_array($notif['type'], ['like', 'comment'])) {
            $notificationData['post'] = [
                'mediaBase64' => $notif['post_media'],
                'likes' => (int)$notif['post_likes'],
                'comments' => (int)$notif['post_comments']
            ];
        }
        
        $results[] = $notificationData;
    }
    
    sendSuccess('Notifications retrieved', [
        'data' => [
            'notifications' => $results,
            'count' => count($results)
        ]
    ]);
    
} catch (PDOException $e) {
    error_log("Get notifications error: " . $e->getMessage());
    sendError('Failed to retrieve notifications', 500);
}
