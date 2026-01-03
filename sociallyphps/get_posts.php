<?php
/**
 * Get Posts API
 * 
 * GET /get_posts.php?userId=xxx (optional - for user's posts or feed)
 * Retrieves posts for feed or specific user
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$userId = isset($_GET['userId']) ? (int)$_GET['userId'] : null;
$targetUserId = isset($_GET['targetUserId']) ? (int)$_GET['targetUserId'] : null;
$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;
$offset = isset($_GET['offset']) ? (int)$_GET['offset'] : 0;

try {
    $pdo = getDbConnection();
    
    if ($targetUserId) {
        // Get posts from specific user
        $stmt = $pdo->prepare("
            SELECT p.*, u.username, u.first_name, u.last_name, u.profile_pic_base64
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE p.user_id = ?
            ORDER BY p.timestamp DESC
            LIMIT ? OFFSET ?
        ");
        $stmt->execute([$targetUserId, $limit, $offset]);
        
    } elseif ($userId) {
        // Get feed posts from followed users
        $stmt = $pdo->prepare("
            SELECT p.*, u.username, u.first_name, u.last_name, u.profile_pic_base64
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE p.user_id IN (
                SELECT following_id FROM following WHERE user_id = ?
            )
            OR p.user_id = ?
            ORDER BY p.timestamp DESC
            LIMIT ? OFFSET ?
        ");
        $stmt->execute([$userId, $userId, $limit, $offset]);
        
    } else {
        // Get all recent posts (public feed)
        $stmt = $pdo->prepare("
            SELECT p.*, u.username, u.first_name, u.last_name, u.profile_pic_base64
            FROM posts p
            JOIN users u ON p.user_id = u.id
            ORDER BY p.timestamp DESC
            LIMIT ? OFFSET ?
        ");
        $stmt->execute([$limit, $offset]);
    }
    
    $posts = [];
    while ($row = $stmt->fetch()) {
        // Debug logging
        // error_log("Processing post ID: " . $row['post_id']);

        // Check if current user liked this post
        $isLiked = false;
        if ($userId) {
            $likeStmt = $pdo->prepare("SELECT id FROM likes WHERE post_id = ? AND user_id = ?");
            $likeStmt->execute([$row['id'], $userId]);
            $isLiked = $likeStmt->fetch() !== false;
        }
        
        $posts[] = [
            'postId' => $row['post_id'],
            'userId' => (int)$row['user_id'],
            'username' => $row['username'],
            'firstName' => $row['first_name'],
            'lastName' => $row['last_name'],
            'profilePicBase64' => $row['profile_pic_base64'],
            'mediaBase64' => $row['media_base64'],
            'mediaType' => $row['media_type'],
            'caption' => $row['caption'],
            'timestamp' => (int)$row['timestamp'],
            'likes' => (int)$row['likes'],
            'commentsCount' => (int)$row['comments_count'],
            'isLiked' => $isLiked
        ];
    }
    
    sendSuccess('Posts retrieved', [
        'data' => [
            'posts' => $posts,
            'count' => count($posts)
        ]
    ]);
    
} catch (PDOException $e) {
    error_log("Get posts error: " . $e->getMessage());
    sendError('Failed to retrieve posts', 500);
}

?>
