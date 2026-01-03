<?php
/**
 * Check Following API
 * 
 * GET /check_following.php?userId=xxx&targetUserId=xxx
 * Checks if user follows another user
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$params = validateGetParams(['userId', 'targetUserId']);

$userId = (int)$params['userId'];
$targetUserId = (int)$params['targetUserId'];

try {
    $pdo = getDbConnection();
    
    $stmt = $pdo->prepare("SELECT id FROM following WHERE user_id = ? AND following_id = ?");
    $stmt->execute([$userId, $targetUserId]);
    
    $isFollowing = $stmt->fetch() !== false;
    
    sendSuccess('Follow status retrieved', [
        'isFollowing' => $isFollowing
    ]);
    
} catch (PDOException $e) {
    error_log("Check following error: " . $e->getMessage());
    sendError('Failed to check following status', 500);
}

?>
