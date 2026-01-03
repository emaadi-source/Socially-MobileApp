<?php
/**
 * Follow User API
 * 
 * POST /follow_user.php
 * Follows a user
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['userId', 'targetUserId']);

$userId = (int)$data['userId'];
$targetUserId = (int)$data['targetUserId'];

if ($userId === $targetUserId) {
    sendError('Cannot follow yourself');
}

try {
    $pdo = getDbConnection();
    
    // Check if already following
    $stmt = $pdo->prepare("SELECT id FROM following WHERE user_id = ? AND following_id = ?");
    $stmt->execute([$userId, $targetUserId]);
    if ($stmt->fetch()) {
        sendError('Already following this user', 409);
    }
    
    $timestamp = getCurrentTimestamp();
    
    // Add to following table
    $stmt = $pdo->prepare("INSERT INTO following (user_id, following_id, timestamp) VALUES (?, ?, ?)");
    $stmt->execute([$userId, $targetUserId, $timestamp]);
    
    // Add to followers table
    $stmt = $pdo->prepare("INSERT INTO followers (user_id, follower_id, timestamp) VALUES (?, ?, ?)");
    $stmt->execute([$targetUserId, $userId, $timestamp]);
    
    sendSuccess('User followed successfully');
    
} catch (PDOException $e) {
    error_log("Follow user error: " . $e->getMessage());
    sendError('Failed to follow user', 500);
}

?>
