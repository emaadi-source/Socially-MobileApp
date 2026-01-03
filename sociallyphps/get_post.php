<?php
/**
 * Get Post API
 * 
 * GET /get_post.php?postId=xxx
 * Retrieves a single post by ID
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$params = validateGetParams(['postId']);
$postId = sanitizeString($params['postId']);
$userId = isset($_GET['userId']) ? (int)$_GET['userId'] : null;

try {
    $pdo = getDbConnection();
    
    $stmt = $pdo->prepare("
        SELECT p.*, u.username, u.first_name, u.last_name, u.profile_pic_base64
        FROM posts p
        JOIN users u ON p.user_id = u.id
        WHERE p.post_id = ?
    ");
    $stmt->execute([$postId]);
    
    $post = $stmt->fetch();
    
    if (!$post) {
        sendError('Post not found', 404);
    }
    
    // Check if current user liked this post
    $isLiked = false;
    if ($userId) {
        $likeStmt = $pdo->prepare("SELECT id FROM likes WHERE post_id = ? AND user_id = ?");
        $likeStmt->execute([$post['id'], $userId]);
        $isLiked = $likeStmt->fetch() !== false;
    }
    
    sendSuccess('Post retrieved', [
        'post' => [
            'postId' => $post['post_id'],
            'userId' => (int)$post['user_id'],
            'username' => $post['username'],
            'firstName' => $post['first_name'],
            'lastName' => $post['last_name'],
            'profilePicBase64' => $post['profile_pic_base64'],
            'mediaBase64' => $post['media_base64'],
            'mediaType' => $post['media_type'],
            'caption' => $post['caption'],
            'timestamp' => (int)$post['timestamp'],
            'likes' => (int)$post['likes'],
            'commentsCount' => (int)$post['comments_count'],
            'isLiked' => $isLiked
        ]
    ]);
    
} catch (PDOException $e) {
    error_log("Get post error: " . $e->getMessage());
    sendError('Failed to retrieve post', 500);
}

?>
