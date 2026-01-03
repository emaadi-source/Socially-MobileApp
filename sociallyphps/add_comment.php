<?php
/**
 * Add Comment API
 * 
 * POST /add_comment.php
 * Adds a comment to a post
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['postId', 'userId', 'text']);

$postId = (int)$data['postId']; // This is the database ID
$userId = (int)$data['userId'];
$text = sanitizeString($data['text']);

try {
    $pdo = getDbConnection();
    
    // postId is already the database ID
    $postDbId = $postId;
    
    // Generate comment ID
    $commentId = generateUniqueId();
    $timestamp = getCurrentTimestamp();
    
    // Insert comment
    $stmt = $pdo->prepare("
        INSERT INTO comments (post_id, user_id, comment_id, text, timestamp)
        VALUES (?, ?, ?, ?, ?)
    ");
    $stmt->execute([$postDbId, $userId, $commentId, $text, $timestamp]);
    
    // Increment comment count
    $stmt = $pdo->prepare("UPDATE posts SET comments_count = comments_count + 1 WHERE id = ?");
    $stmt->execute([$postDbId]);
    
    // Get post owner
    $stmt = $pdo->prepare("SELECT user_id FROM posts WHERE id = ?");
    $stmt->execute([$postDbId]);
    $postOwnerId = $stmt->fetch()['user_id'];
    
    // Don't send notification if user commented on their own post
    if ($postOwnerId != $userId) {
        // Check if notification already exists
        $stmt = $pdo->prepare("SELECT id FROM notifications WHERE user_id = ? AND post_id = ? AND type = 'comment'");
        $stmt->execute([$postOwnerId, $postDbId]);
        $existingNotification = $stmt->fetch();
        
        if (!$existingNotification) {
            // Create new comment notification (first comment)
            $stmt = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, post_id, type, timestamp, is_read) VALUES (?, ?, ?, 'comment', ?, 0)");
            $stmt->execute([$postOwnerId, $userId, $postDbId, $timestamp]);
        } else {
            // Update existing notification timestamp to bring it to top
            $stmt = $pdo->prepare("UPDATE notifications SET timestamp = ?, is_read = 0 WHERE id = ?");
            $stmt->execute([$timestamp, $existingNotification['id']]);
        }
    }
    
    sendSuccess('Comment added', [
        'commentId' => $commentId,
        'timestamp' => $timestamp
    ]);
    
} catch (PDOException $e) {
    error_log("Add comment error: " . $e->getMessage());
    sendError('Failed to add comment', 500);
}

?>
