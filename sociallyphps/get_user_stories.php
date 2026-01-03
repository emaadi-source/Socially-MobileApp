<?php
/**
 * Get User Stories API
 * 
 * GET /get_user_stories.php?userId=xxx
 * Retrieves all active stories for a specific user
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$params = validateGetParams(['userId']);
$userId = (int)$params['userId'];

try {
    $pdo = getDbConnection();
    
    $twentyFourHoursAgo = (time() * 1000) - (24 * 60 * 60 * 1000);
    
    $stmt = $pdo->prepare("
        SELECT s.*, u.username, u.profile_pic_base64
        FROM stories s
        JOIN users u ON s.user_id = u.id
        WHERE s.user_id = ? AND s.timestamp > ?
        ORDER BY s.timestamp ASC
    ");
    
    $stmt->execute([$userId, $twentyFourHoursAgo]);
    
    $stories = [];
    while ($row = $stmt->fetch()) {
        $stories[] = [
            'id' => (int)$row['id'],
            'storyId' => $row['story_id'],
            'mediaBase64' => $row['media_base64'],
            'mediaType' => $row['media_type'],
            'timestamp' => (int)$row['timestamp'],
            'username' => $row['username'],
            'profilePicBase64' => $row['profile_pic_base64']
        ];
    }
    
    sendSuccess('User stories retrieved', ['stories' => $stories]);
    
} catch (PDOException $e) {
    error_log("Get user stories error: " . $e->getMessage());
    sendError('Failed to retrieve user stories', 500);
}
?>
