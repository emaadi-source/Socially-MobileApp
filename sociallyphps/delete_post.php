<?php
/**
 * Delete Post API
 * 
 * POST /delete_post.php
 * Deletes a post (owner only)
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['postId', 'userId']);

$postId = sanitizeString($data['postId']);
$userId = (int)$data['userId'];

try {
    $pdo = getDbConnection();
    
    // Get post and verify ownership
    $stmt = $pdo->prepare("SELECT id, user_id FROM posts WHERE post_id = ?");
    $stmt->execute([$postId]);
    $post = $stmt->fetch();
    
    if (!$post) {
        sendError('Post not found', 404);
    }
    
    if ((int)$post['user_id'] !== $userId) {
        sendError('Unauthorized. You can only delete your own posts', 403);
    }
    
    // Delete post (cascade will delete likes and comments)
    $stmt = $pdo->prepare("DELETE FROM posts WHERE id = ?");
    $stmt->execute([$post['id']]);
    
    sendSuccess('Post deleted successfully');
    
} catch (PDOException $e) {
    error_log("Delete post error: " . $e->getMessage());
    sendError('Failed to delete post', 500);
}

?>
