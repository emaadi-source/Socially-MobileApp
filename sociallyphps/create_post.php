<?php
/**
 * Create Post API
 * 
 * POST /create_post.php
 * Creates a new post with media
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['userId', 'mediaBase64', 'mediaType', 'caption']);

// Debug logging
error_log("Create Post Request: " . print_r($data, true));
error_log("Media Base64 Length: " . strlen($data['mediaBase64']));


$userId = (int)$data['userId'];
$mediaBase64 = $data['mediaBase64'];
$mediaType = sanitizeString($data['mediaType']);
$caption = sanitizeString($data['caption']);

try {
    $pdo = getDbConnection();
    
    // Verify user exists
    $user = getUserById($pdo, $userId);
    if (!$user) {
        sendError('User not found', 404);
    }
    
    // Generate unique post ID
    $postId = generateUniqueId();
    $timestamp = getCurrentTimestamp();
    
    // Insert post
    $stmt = $pdo->prepare("
        INSERT INTO posts (user_id, post_id, media_base64, media_type, caption, timestamp, likes, comments_count)
        VALUES (?, ?, ?, ?, ?, ?, 0, 0)
    ");
    
    $stmt->execute([$userId, $postId, $mediaBase64, $mediaType, $caption, $timestamp]);
    
    sendSuccess('Post created successfully', [
        'postId' => $postId,
        'timestamp' => $timestamp
    ]);
    
} catch (PDOException $e) {
    error_log("Create post error: " . $e->getMessage());
    sendError('Failed to create post', 500);
}

?>
