<?php
/**
 * Get Feed API
 * 
 * GET /get_feed.php?userId=xxx&limit=20&offset=0
 * Returns posts from users that the current user follows (accepted status only)
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
$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 20;
$offset = isset($_GET['offset']) ? (int)$_GET['offset'] : 0;

if (!$userId) {
    sendError('userId is required');
}

try {
    $pdo = getDbConnection();
    
    // Get posts from followed users (accepted status only)
    $stmt = $pdo->prepare("
        SELECT 
            p.*,
            u.username,
            u.first_name,
            u.last_name,
            u.profile_pic_base64
        FROM posts p
        JOIN users u ON p.user_id = u.id
        JOIN following f ON p.user_id = f.following_id
        WHERE f.user_id = ? AND f.status = 'accepted'
        ORDER BY p.timestamp DESC
        LIMIT ? OFFSET ?
    ");
    $stmt->execute([$userId, $limit, $offset]);
    
    $posts = [];
    while ($row = $stmt->fetch()) {
        // Check if current user liked this post
        $likeStmt = $pdo->prepare("SELECT id FROM likes WHERE post_id = ? AND user_id = ?");
        $likeStmt->execute([$row['id'], $userId]);
        $isLiked = $likeStmt->fetch() !== false;
        
        $posts[] = [
            'id' => (int)$row['id'],
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
    
    sendSuccess('Feed retrieved', [
        'posts' => $posts,
        'count' => count($posts)
    ]);
    
} catch (PDOException $e) {
    error_log("Get feed error: " . $e->getMessage());
    sendError('Failed to retrieve feed', 500);
}
