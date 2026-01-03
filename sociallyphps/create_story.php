<?php
/**
 * Create Story API
 * 
 * POST /create_story.php
 * Creates a new story with media chunks
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['userId', 'mediaType', 'chunkCount']);

$userId = (int)$data['userId'];
$mediaType = sanitizeString($data['mediaType']);
$chunkCount = (int)$data['chunkCount'];
$chunks = isset($data['chunks']) ? $data['chunks'] : [];

if (count($chunks) !== $chunkCount) {
    sendError('Chunk count mismatch');
}

try {
    $pdo = getDbConnection();
    $pdo->beginTransaction();
    
    // Generate story ID
    $storyId = generateUniqueId();
    $timestamp = getCurrentTimestamp();
    
    // Insert story metadata
    $stmt = $pdo->prepare("
        INSERT INTO stories (user_id, story_id, media_type, timestamp, chunk_count)
        VALUES (?, ?, ?, ?, ?)
    ");
    $stmt->execute([$userId, $storyId, $mediaType, $timestamp, $chunkCount]);
    
    $storyDbId = $pdo->lastInsertId();
    
    // Insert chunks
    $stmt = $pdo->prepare("
        INSERT INTO story_chunks (story_id, chunk_index, chunk_data)
        VALUES (?, ?, ?)
    ");
    
    for ($i = 0; $i < $chunkCount; $i++) {
        $stmt->execute([$storyDbId, $i, $chunks[$i]]);
    }
    
    $pdo->commit();
    
    sendSuccess('Story created successfully', [
        'storyId' => $storyId,
        'timestamp' => $timestamp
    ]);
    
} catch (PDOException $e) {
    $pdo->rollBack();
    error_log("Create story error: " . $e->getMessage());
    sendError('Failed to create story', 500);
}

?>
