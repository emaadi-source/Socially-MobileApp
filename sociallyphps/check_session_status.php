<?php
/**
 * Check Session Status API
 * 
 * GET /check_session_status.php?deviceId=xxx
 * Checks if device has a session and returns user info
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

$deviceId = isset($_GET['deviceId']) ? sanitizeString($_GET['deviceId']) : null;

if (!$deviceId) {
    sendError('deviceId is required');
}

try {
    $pdo = getDbConnection();
    
    // Get most recent session for this device
    $stmt = $pdo->prepare("
        SELECT s.*, u.username, u.email, u.profile_pic_base64
        FROM sessions s
        JOIN users u ON s.user_id = u.id
        WHERE s.device_id = ?
        ORDER BY s.updated_at DESC
        LIMIT 1
    ");
    $stmt->execute([$deviceId]);
    $session = $stmt->fetch();
    
    if (!$session) {
        sendSuccess('No session found', [
            'data' => [
                'hasSession' => false,
                'isLoggedIn' => false
            ]
        ]);
    } else {
        sendSuccess('Session found', [
            'data' => [
                'hasSession' => true,
                'isLoggedIn' => (bool)$session['is_logged_in'],
                'isActive' => (bool)$session['is_active'],
                'userId' => (int)$session['user_id'],
                'username' => $session['username'],
                'email' => $session['email'],
                'profilePicBase64' => $session['profile_pic_base64']
            ]
        ]);
    }
    
} catch (PDOException $e) {
    error_log("Check session error: " . $e->getMessage());
    sendError('Failed to check session', 500);
}
