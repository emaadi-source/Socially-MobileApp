<?php
/**
 * Check Follow Status API
 * 
 * GET /check_follow_status.php?userId=xxx&targetUserId=xxx
 * Returns the follow status between two users
 */

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

$userId = isset($_GET['userId']) ? (int)$_GET['userId'] : null;
$targetUserId = isset($_GET['targetUserId']) ? (int)$_GET['targetUserId'] : null;

if (!$userId || !$targetUserId) {
    sendError('userId and targetUserId are required');
}

try {
    $pdo = getDbConnection();
    
    // Check if user is following target
    $stmt = $pdo->prepare("SELECT status FROM following WHERE user_id = ? AND following_id = ?");
    $stmt->execute([$userId, $targetUserId]);
    $following = $stmt->fetch();
    
    $status = 'none'; // none, pending, following
    if ($following) {
        $status = $following['status'] === 'pending' ? 'pending' : 'following';
    }
    
    sendSuccess('Follow status retrieved', [
        'data' => [
            'status' => $status
        ]
    ]);
    
} catch (PDOException $e) {
    error_log("Check follow status error: " . $e->getMessage());
    sendError('Failed to check follow status', 500);
}
