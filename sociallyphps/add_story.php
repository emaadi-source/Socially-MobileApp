<?php
/**
 * Add Story API
 * 
 * POST /add_story.php
 * Adds a new story (image/video)
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['userId', 'mediaBase64', 'mediaType']);

$userId = (int)$data['userId'];
$mediaBase64 = $data['mediaBase64'];
$mediaType = sanitizeString($data['mediaType']);

try {
    $pdo = getDbConnection();
    
    $storyId = generateUniqueId();
    $timestamp = getCurrentTimestamp();
    
    // Insert story directly with media
    $stmt = $pdo->prepare("
        INSERT INTO stories (user_id, story_id, media_base64, media_type, timestamp, chunk_count)
        VALUES (?, ?, ?, ?, ?, 1)
    ");
    
    $stmt->execute([$userId, $storyId, $mediaBase64, $mediaType, $timestamp]);
    
    sendSuccess('Story uploaded successfully', ['storyId' => $storyId]);
    
} catch (PDOException $e) {
    error_log("Add story error: " . $e->getMessage());
    sendError('Failed to upload story', 500);
}
?>
