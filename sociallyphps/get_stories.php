<?php
/**
 * Get Stories API
 * 
 * GET /get_stories.php?userId=xxx
 * Retrieves active stories from followed users
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$params = validateGetParams(['userId']);
$userId = (int)$params['userId'];

try {
    $pdo = getDbConnection();
    
    // Calculate 24 hours ago timestamp
    $twentyFourHoursAgo = (time() * 1000) - (24 * 60 * 60 * 1000);
    
    // Get users who have active stories (self + followed)
    // Group by user to show one circle per user
    $stmt = $pdo->prepare("
        SELECT DISTINCT u.id, u.username, u.profile_pic_base64
        FROM users u
        JOIN stories s ON u.id = s.user_id
        WHERE (u.id = ? OR u.id IN (SELECT user_id FROM followers WHERE follower_id = ?))
        AND s.timestamp > ?
        ORDER BY s.timestamp DESC
    ");
    
    $stmt->execute([$userId, $userId, $twentyFourHoursAgo]);
    
    $usersWithStories = [];
    while ($row = $stmt->fetch()) {
        $usersWithStories[] = [
            'userId' => (int)$row['id'],
            'username' => $row['username'],
            'profilePicBase64' => $row['profile_pic_base64'],
            'hasUnseen' => true // Simplified for now
        ];
    }
    
    sendSuccess('Stories retrieved', ['users' => $usersWithStories]);
    
} catch (PDOException $e) {
    error_log("Get stories error: " . $e->getMessage());
    sendError('Failed to retrieve stories', 500);
}
?>
