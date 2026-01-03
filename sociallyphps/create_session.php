<?php
/**
 * Create Session API
 * 
 * POST /create_session.php
 * Creates a new user session
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['userId', 'deviceId']);

$userId = (int)$data['userId'];
$deviceId = sanitizeString($data['deviceId']);

try {
    $pdo = getDbConnection();
    
    // Generate session ID
    $sessionId = generateSessionId();
    $timestamp = getCurrentTimestamp();
    
    // Insert session
    $stmt = $pdo->prepare("
        INSERT INTO sessions (user_id, session_id, device_id, is_active, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?)
    ");
    $stmt->execute([$userId, $sessionId, $deviceId, true, $timestamp, $timestamp]);
    
    sendSuccess('Session created', [
        'sessionId' => $sessionId
    ]);
    
} catch (PDOException $e) {
    error_log("Create session error: " . $e->getMessage());
    sendError('Failed to create session', 500);
}

?>
