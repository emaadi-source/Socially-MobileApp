<?php
/**
 * Get Active Sessions API
 * 
 * GET /get_active_sessions.php?userId=xxx
 * Retrieves active sessions for a user
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$params = validateGetParams(['userId']);
$userId = (int)$params['userId'];

try {
    $pdo = getDbConnection();
    
    $stmt = $pdo->prepare("
        SELECT session_id, device_id, created_at, updated_at
        FROM sessions
        WHERE user_id = ? AND is_active = ?
        ORDER BY updated_at DESC
    ");
    $stmt->execute([$userId, true]);
    
    $sessions = [];
    while ($row = $stmt->fetch()) {
        $sessions[] = [
            'sessionId' => $row['session_id'],
            'deviceId' => $row['device_id'],
            'createdAt' => (int)$row['created_at'],
            'updatedAt' => (int)$row['updated_at']
        ];
    }
    
    sendSuccess('Active sessions retrieved', [
        'sessions' => $sessions,
        'count' => count($sessions)
    ]);
    
} catch (PDOException $e) {
    error_log("Get active sessions error: " . $e->getMessage());
    sendError('Failed to retrieve sessions', 500);
}

?>
