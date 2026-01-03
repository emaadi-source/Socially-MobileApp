<?php
/**
 * Get Following API
 * 
 * GET /get_following.php?userId=xxx
 * Retrieves list of users that the specified user follows
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$params = validateGetParams(['userId']);
$userId = (int)$params['userId'];

try {
    $pdo = getDbConnection();
    
    $stmt = $pdo->prepare("
        SELECT u.id, u.email, u.username, u.first_name, u.last_name, u.profile_pic_base64
        FROM users u
        JOIN following f ON u.id = f.following_id
        WHERE f.user_id = ?
        ORDER BY f.timestamp DESC
    ");
    $stmt->execute([$userId]);
    
    $following = [];
    while ($row = $stmt->fetch()) {
        $following[] = [
            'id' => (int)$row['id'],
            'email' => $row['email'],
            'username' => $row['username'],
            'firstName' => $row['first_name'],
            'lastName' => $row['last_name'],
            'profilePicBase64' => $row['profile_pic_base64']
        ];
    }
    
    sendSuccess('Following retrieved', [
        'following' => $following,
        'count' => count($following)
    ]);
    
} catch (PDOException $e) {
    error_log("Get following error: " . $e->getMessage());
    sendError('Failed to retrieve following', 500);
}

?>
