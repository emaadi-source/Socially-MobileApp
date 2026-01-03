<?php
/**
 * Check Like API
 * 
 * GET /check_like.php?postId=xxx&userId=xxx
 * Checks if user has liked a post
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$params = validateGetParams(['postId', 'userId']);

$postId = sanitizeString($params['postId']);
$userId = (int)$params['userId'];

try {
    $pdo = getDbConnection();
    
    // Get post database ID
    $postDbId = getPostDbId($pdo, $postId);
    if (!$postDbId) {
        sendError('Post not found', 404);
    }
    
    $stmt = $pdo->prepare("SELECT id FROM likes WHERE post_id = ? AND user_id = ?");
    $stmt->execute([$postDbId, $userId]);
    
    $liked = $stmt->fetch() !== false;
    
    sendSuccess('Like status retrieved', [
        'liked' => $liked
    ]);
    
} catch (PDOException $e) {
    error_log("Check like error: " . $e->getMessage());
    sendError('Failed to check like status', 500);
}

?>
