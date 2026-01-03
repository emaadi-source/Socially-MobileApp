<?php
/**
 * Respond to Follow Request API
 * 
 * POST /respond_follow_request.php
 * Body: {userId, senderId, action: 'accept'|'reject'}
 * Accept or reject a follow request
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

checkRequestMethod('POST');

$data = validatePostParams(['userId', 'senderId', 'action']);

$userId = (int)$data['userId'];
$senderId = (int)$data['senderId'];
$action = $data['action'];

if (!in_array($action, ['accept', 'reject'])) {
    sendError('Invalid action. Must be accept or reject');
}

try {
    $pdo = getDbConnection();
    $timestamp = getCurrentTimestamp();
    
    if ($action === 'accept') {
        // Update status to accepted in both tables
        $stmt = $pdo->prepare("UPDATE followers SET status = 'accepted' WHERE user_id = ? AND follower_id = ? AND status = 'pending'");
        $stmt->execute([$userId, $senderId]);
        
        $stmt = $pdo->prepare("UPDATE following SET status = 'accepted' WHERE user_id = ? AND following_id = ? AND status = 'pending'");
        $stmt->execute([$senderId, $userId]);
        
        // Delete the follow_request notification (it goes away)
        $stmt = $pdo->prepare("DELETE FROM notifications WHERE user_id = ? AND sender_id = ? AND type = 'follow_request'");
        $stmt->execute([$userId, $senderId]);
        
        // Create "You started following" notification for sender (lifelong, is_read=false)
        $stmt = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, timestamp, is_read) VALUES (?, ?, 'follow_accept', ?, 0)");
        $stmt->execute([$senderId, $userId, $timestamp]);
        
        // Check if target user (B) is following back the sender (A)
        $stmt = $pdo->prepare("SELECT status FROM following WHERE user_id = ? AND following_id = ?");
        $stmt->execute([$userId, $senderId]);
        $isFollowingBack = $stmt->fetch();
        
        // If B is not following A, create a "follow_back" notification for B
        if (!$isFollowingBack || $isFollowingBack['status'] !== 'accepted') {
            $stmt = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, timestamp, is_read) VALUES (?, ?, 'follow_back', ?, 0)");
            $stmt->execute([$userId, $senderId, $timestamp]);
        } else {
            // B is already following A, so delete the follow_back notification (button should disappear)
            $stmt = $pdo->prepare("DELETE FROM notifications WHERE user_id = ? AND sender_id = ? AND type = 'follow_back'");
            $stmt->execute([$userId, $senderId]);
        }
        
        sendSuccess('Follow request accepted');
        
    } else {
        // Delete pending request from both tables
        $stmt = $pdo->prepare("DELETE FROM followers WHERE user_id = ? AND follower_id = ? AND status = 'pending'");
        $stmt->execute([$userId, $senderId]);
        
        $stmt = $pdo->prepare("DELETE FROM following WHERE user_id = ? AND following_id = ? AND status = 'pending'");
        $stmt->execute([$senderId, $userId]);
        
        // Delete the follow_request notification (it goes away)
        $stmt = $pdo->prepare("DELETE FROM notifications WHERE user_id = ? AND sender_id = ? AND type = 'follow_request'");
        $stmt->execute([$userId, $senderId]);
        
        sendSuccess('Follow request rejected');
    }
    
} catch (PDOException $e) {
    error_log("Respond follow error: " . $e->getMessage());
    sendError('Failed to respond to follow request', 500);
}
