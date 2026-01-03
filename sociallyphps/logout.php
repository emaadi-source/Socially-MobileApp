<?php
/**
 * User Logout API
 * 
 * POST /logout.php
 * Logs out user and deactivates sessions
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

$data = validatePostParams(['userId']);
$userId = (int)$data['userId'];

try {
    $pdo = getDbConnection();
    
    // Update user login status
    $stmt = $pdo->prepare("UPDATE users SET is_logged_in = ? WHERE id = ?");
    $stmt->execute([false, $userId]);
    
    // Update sessions: set is_logged_in to false but keep session for page3
    $stmt = $pdo->prepare("UPDATE sessions SET is_active = ?, is_logged_in = ? WHERE user_id = ?");
    $stmt->execute([false, false, $userId]);
    
    sendSuccess('Logout successful');
    
} catch (PDOException $e) {
    error_log("Logout error: " . $e->getMessage());
    sendError('Logout failed', 500);
}

?>
