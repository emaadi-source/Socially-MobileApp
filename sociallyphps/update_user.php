<?php
/**
 * Update User Profile API
 * 
 * POST /update_user.php
 * Updates user profile information
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

checkRequestMethod('POST');

$data = validatePostParams(['userId']);

$userId = (int)$data['userId'];
$username = isset($data['username']) ? sanitizeString($data['username']) : null;
$firstName = isset($data['firstName']) ? sanitizeString($data['firstName']) : null;
$lastName = isset($data['lastName']) ? sanitizeString($data['lastName']) : null;
$bio = isset($data['bio']) ? sanitizeString($data['bio']) : null;
$profilePicBase64 = isset($data['profilePicBase64']) ? $data['profilePicBase64'] : null;

try {
    $pdo = getDbConnection();
    
    // Get current user data
    $stmt = $pdo->prepare("SELECT username FROM users WHERE id = ?");
    $stmt->execute([$userId]);
    $currentUser = $stmt->fetch();
    
    if (!$currentUser) {
        sendError('User not found', 404);
    }
    
    // Check if username is being changed and if it's available
    if ($username && $username !== $currentUser['username']) {
        $stmt = $pdo->prepare("SELECT id FROM users WHERE username = ? AND id != ?");
        $stmt->execute([$username, $userId]);
        if ($stmt->fetch()) {
            sendError('Username already taken', 409);
        }
    }
    
    // Build update query dynamically
    $updates = [];
    $params = [];
    
    if ($username) {
        $updates[] = "username = ?";
        $params[] = $username;
    }
    if ($firstName) {
        $updates[] = "first_name = ?";
        $params[] = $firstName;
    }
    if ($lastName) {
        $updates[] = "last_name = ?";
        $params[] = $lastName;
    }
    if ($bio !== null) {
        $updates[] = "bio = ?";
        $params[] = $bio;
    }
    if ($profilePicBase64) {
        $updates[] = "profile_pic_base64 = ?";
        $params[] = $profilePicBase64;
    }
    
    if (empty($updates)) {
        sendError('No fields to update');
    }
    
    $params[] = $userId;
    $sql = "UPDATE users SET " . implode(", ", $updates) . " WHERE id = ?";
    
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    
    // Get updated user data
    $stmt = $pdo->prepare("
        SELECT id, email, username, first_name, last_name, bio, profile_pic_base64
        FROM users WHERE id = ?
    ");
    $stmt->execute([$userId]);
    $user = $stmt->fetch();
    
    sendSuccess('Profile updated successfully', [
        'user' => [
            'id' => (int)$user['id'],
            'email' => $user['email'],
            'username' => $user['username'],
            'firstName' => $user['first_name'],
            'lastName' => $user['last_name'],
            'bio' => $user['bio'] ?? '',
            'profilePicBase64' => $user['profile_pic_base64']
        ]
    ]);
    
} catch (PDOException $e) {
    error_log("Update user error: " . $e->getMessage());
    sendError('Failed to update profile', 500);
}

?>
