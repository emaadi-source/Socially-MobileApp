<?php
/**
 * Get User API
 * 
 * GET /get_user.php?email=xxx OR ?username=xxx OR ?userId=xxx
 * Retrieves user profile data
 */

// Add CORS headers
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

$email = isset($_GET['email']) ? sanitizeString($_GET['email']) : null;
$username = isset($_GET['username']) ? sanitizeString($_GET['username']) : null;
$userId = isset($_GET['userId']) ? (int)$_GET['userId'] : null;

if (!$email && !$username && !$userId) {
    sendError('Either email, username, or userId is required');
}

try {
    $pdo = getDbConnection();
    
    // Build query based on provided parameter
    if ($userId) {
        $stmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
        $stmt->execute([$userId]);
    } elseif ($email) {
        $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
        $stmt->execute([$email]);
    } else {
        $stmt = $pdo->prepare("SELECT * FROM users WHERE username = ?");
        $stmt->execute([$username]);
    }
    
    $user = $stmt->fetch();
    
    if (!$user) {
        sendError('User not found', 404);
    }
    
    // Get follower and following counts (only accepted)
    $stmt = $pdo->prepare("SELECT COUNT(*) as count FROM followers WHERE user_id = ? AND status = 'accepted'");
    $stmt->execute([$user['id']]);
    $followersCount = $stmt->fetch()['count'];
    
    $stmt = $pdo->prepare("SELECT COUNT(*) as count FROM following WHERE user_id = ? AND status = 'accepted'");
    $stmt->execute([$user['id']]);
    $followingCount = $stmt->fetch()['count'];
    
    // Get posts count
    $stmt = $pdo->prepare("SELECT COUNT(*) as count FROM posts WHERE user_id = ?");
    $stmt->execute([$user['id']]);
    $postsCount = $stmt->fetch()['count'];
    
    sendSuccess('User found', [
        'data' => [
            'user' => [
                'id' => (int)$user['id'],
                'email' => $user['email'],
                'username' => $user['username'],
                'firstName' => $user['first_name'],
                'lastName' => $user['last_name'],
                'bio' => $user['bio'] ?? '',
                'dob' => $user['dob'],
                'profilePicBase64' => $user['profile_pic_base64'],
                'deviceId' => $user['device_id'],
                'lastLogin' => (int)$user['last_login'],
                'isLoggedIn' => (bool)$user['is_logged_in'],
                'followersCount' => (int)$followersCount,
                'followingCount' => (int)$followingCount,
                'postsCount' => (int)$postsCount
            ]
        ]
    ]);
    
} catch (PDOException $e) {
    error_log("Get user error: " . $e->getMessage());
    sendError('Failed to retrieve user', 500);
}

?>
