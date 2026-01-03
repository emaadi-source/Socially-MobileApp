<?php
/**
 * Update Session API
 * 
 * POST /update_session.php
 * Updates session active status
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['sessionId', 'isActive']);

$sessionId = sanitizeString($data['sessionId']);
$isActive = (bool)$data['isActive'];

try {
    $pdo = getDbConnection();
    
    $timestamp = getCurrentTimestamp();
    
    $stmt = $pdo->prepare("UPDATE sessions SET is_active = ?, updated_at = ? WHERE session_id = ?");
    $stmt->execute([$isActive, $timestamp, $sessionId]);
    
    if ($stmt->rowCount() === 0) {
        sendError('Session not found', 404);
    }
    
    sendSuccess('Session updated successfully');
    
} catch (PDOException $e) {
    error_log("Update session error: " . $e->getMessage());
    sendError('Failed to update session', 500);
}

?>
