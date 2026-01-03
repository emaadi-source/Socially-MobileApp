<?php
/**
 * Unfollow User API
 * 
 * POST /unfollow_user.php
 * Unfollows a user
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['userId', 'targetUserId']);

$userId = (int)$data['userId'];
$targetUserId = (int)$data['targetUserId'];

try {
    $pdo = getDbConnection();
    
    // Remove from following table
    $stmt = $pdo->prepare("DELETE FROM following WHERE user_id = ? AND following_id = ?");
    $stmt->execute([$userId, $targetUserId]);
    
    // Remove from followers table
    $stmt = $pdo->prepare("DELETE FROM followers WHERE user_id = ? AND follower_id = ?");
    $stmt->execute([$targetUserId, $userId]);
    
    // Delete any follow_back notifications for this relationship
    $stmt = $pdo->prepare("DELETE FROM notifications WHERE user_id = ? AND sender_id = ? AND type = 'follow_back'");
    $stmt->execute([$targetUserId, $userId]);
    
    // Delete "You started following" notification for the person who unfollowed
    $stmt = $pdo->prepare("DELETE FROM notifications WHERE user_id = ? AND sender_id = ? AND type = 'follow_accept'");
    $stmt->execute([$userId, $targetUserId]);
    
    sendSuccess('User unfollowed successfully');
    
} catch (PDOException $e) {
    error_log("Unfollow user error: " . $e->getMessage());
    sendError('Failed to unfollow user', 500);
}

?>
