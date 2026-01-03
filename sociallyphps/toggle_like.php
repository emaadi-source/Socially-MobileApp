<?php
/**
 * Toggle Like API
 * 
 * POST /toggle_like.php
 * Likes or unlikes a post
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['postId', 'userId']);

$postId = (int)$data['postId']; // This is the database ID
$userId = (int)$data['userId'];

try {
    $pdo = getDbConnection();
    
    // postId is already the database ID from get_feed.php
    $postDbId = $postId;
    
    // Check if already liked
    $stmt = $pdo->prepare("SELECT id FROM likes WHERE post_id = ? AND user_id = ?");
    $stmt->execute([$postDbId, $userId]);
    $existingLike = $stmt->fetch();
    
    $timestamp = getCurrentTimestamp();
    
    if ($existingLike) {
        // Unlike - remove like
        $stmt = $pdo->prepare("DELETE FROM likes WHERE post_id = ? AND user_id = ?");
        $stmt->execute([$postDbId, $userId]);
        
        // Decrement like count
        $stmt = $pdo->prepare("UPDATE posts SET likes = likes - 1 WHERE id = ?");
        $stmt->execute([$postDbId]);
        
        // Get post owner
        $stmt = $pdo->prepare("SELECT user_id FROM posts WHERE id = ?");
        $stmt->execute([$postDbId]);
        $postOwnerId = $stmt->fetch()['user_id'];
        
        // Don't send notification if user liked their own post
        if ($postOwnerId != $userId) {
            // Check if there's an existing like notification
            $stmt = $pdo->prepare("SELECT id FROM notifications WHERE user_id = ? AND post_id = ? AND type = 'like'");
            $stmt->execute([$postOwnerId, $postDbId]);
            $notification = $stmt->fetch();
            
            if ($notification) {
                // Get remaining likes count
                $stmt = $pdo->prepare("SELECT COUNT(*) as count FROM likes WHERE post_id = ?");
                $stmt->execute([$postDbId]);
                $remainingLikes = $stmt->fetch()['count'];
                
                if ($remainingLikes == 0) {
                    // No more likes, delete notification
                    $stmt = $pdo->prepare("DELETE FROM notifications WHERE id = ?");
                    $stmt->execute([$notification['id']]);
                } else {
                    // Update notification with new first liker
                    $stmt = $pdo->prepare("SELECT user_id FROM likes WHERE post_id = ? ORDER BY timestamp DESC LIMIT 1");
                    $stmt->execute([$postDbId]);
                    $newFirstLiker = $stmt->fetch()['user_id'];
                    
                    $stmt = $pdo->prepare("UPDATE notifications SET sender_id = ?, timestamp = ? WHERE id = ?");
                    $stmt->execute([$newFirstLiker, $timestamp, $notification['id']]);
                }
            }
        }
        
        $liked = false;
        
    } else {
        // Like - add like
        $stmt = $pdo->prepare("INSERT INTO likes (post_id, user_id, timestamp) VALUES (?, ?, ?)");
        $stmt->execute([$postDbId, $userId, $timestamp]);
        
        // Increment like count
        $stmt = $pdo->prepare("UPDATE posts SET likes = likes + 1 WHERE id = ?");
        $stmt->execute([$postDbId]);
        
        // Get post owner
        $stmt = $pdo->prepare("SELECT user_id FROM posts WHERE id = ?");
        $stmt->execute([$postDbId]);
        $postOwnerId = $stmt->fetch()['user_id'];
        
        // Don't send notification if user liked their own post
        if ($postOwnerId != $userId) {
            // Check if notification already exists
            $stmt = $pdo->prepare("SELECT id FROM notifications WHERE user_id = ? AND post_id = ? AND type = 'like'");
            $stmt->execute([$postOwnerId, $postDbId]);
            $existingNotification = $stmt->fetch();
            
            if (!$existingNotification) {
                // Create new like notification (first like)
                $stmt = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, post_id, type, timestamp, is_read) VALUES (?, ?, ?, 'like', ?, 0)");
                $stmt->execute([$postOwnerId, $userId, $postDbId, $timestamp]);
            } else {
                // Update existing notification timestamp to bring it to top
                $stmt = $pdo->prepare("UPDATE notifications SET timestamp = ?, is_read = 0 WHERE id = ?");
                $stmt->execute([$timestamp, $existingNotification['id']]);
            }
        }
        
        $liked = true;
    }
    
    // Get updated like count
    $stmt = $pdo->prepare("SELECT likes FROM posts WHERE id = ?");
    $stmt->execute([$postDbId]);
    $likeCount = $stmt->fetch()['likes'];
    
    sendSuccess($liked ? 'Post liked' : 'Post unliked', [
        'liked' => $liked,
        'likeCount' => (int)$likeCount
    ]);
    
} catch (PDOException $e) {
    error_log("Toggle like error: " . $e->getMessage());
    sendError('Failed to toggle like', 500);
}

?>
