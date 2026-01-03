<?php
/**
 * Update FCM Token API
 * 
 * POST /update_fcm_token.php
 * Updates FCM token for push notifications
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['userId', 'fcmToken']);

$userId = (int)$data['userId'];
$fcmToken = sanitizeString($data['fcmToken']);

try {
    $pdo = getDbConnection();
    
    // Check if token exists for user
    $stmt = $pdo->prepare("SELECT id FROM user_tokens WHERE user_id = ?");
    $stmt->execute([$userId]);
    
    if ($stmt->fetch()) {
        // Update existing token
        $stmt = $pdo->prepare("UPDATE user_tokens SET fcm_token = ? WHERE user_id = ?");
        $stmt->execute([$fcmToken, $userId]);
    } else {
        // Insert new token
        $stmt = $pdo->prepare("INSERT INTO user_tokens (user_id, fcm_token) VALUES (?, ?)");
        $stmt->execute([$userId, $fcmToken]);
    }
    
    sendSuccess('FCM token updated successfully');
    
} catch (PDOException $e) {
    error_log("Update FCM token error: " . $e->getMessage());
    sendError('Failed to update FCM token', 500);
}

?>
