<?php
/**
 * Get Comments API
 * 
 * GET /get_comments.php?postId=xxx
 * Retrieves comments for a post
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$params = validateGetParams(['postId']);
$postId = (int)$params['postId']; // This is the database ID

try {
    $pdo = getDbConnection();
    
    // postId is already the database ID
    $postDbId = $postId;
    
    // Get comments with user info (chronological order - oldest first, latest at bottom)
    $stmt = $pdo->prepare("
        SELECT c.*, u.username, u.first_name, u.last_name, u.profile_pic_base64, u.email
        FROM comments c
        JOIN users u ON c.user_id = u.id
        WHERE c.post_id = ?
        ORDER BY c.timestamp ASC
    ");
    $stmt->execute([$postDbId]);
    
    $comments = [];
    while ($row = $stmt->fetch()) {
        $comments[] = [
            'commentId' => $row['comment_id'],
            'userId' => (int)$row['user_id'],
            'username' => $row['username'],
            'firstName' => $row['first_name'],
            'lastName' => $row['last_name'],
            'profilePicBase64' => $row['profile_pic_base64'],
            'email' => $row['email'],
            'text' => $row['text'],
            'timestamp' => (int)$row['timestamp']
        ];
    }
    
    sendSuccess('Comments retrieved', [
        'comments' => $comments,
        'count' => count($comments)
    ]);
    
} catch (PDOException $e) {
    error_log("Get comments error: " . $e->getMessage());
    sendError('Failed to retrieve comments', 500);
}

?>
