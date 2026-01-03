<?php
/**
 * Check Username API
 * 
 * GET /check_username.php?username=xxx
 * Checks if username is available
 */

// Add CORS headers
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');
header('Content-Type: application/json');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('GET');

$params = validateGetParams(['username']);
$username = sanitizeString($params['username']);

try {
    $pdo = getDbConnection();
    
    $stmt = $pdo->prepare("SELECT id FROM users WHERE username = ?");
    $stmt->execute([$username]);
    
    $available = $stmt->fetch() === false;
    
    sendSuccess('Username availability checked', [
        'available' => $available,
        'username' => $username
    ]);
    
} catch (PDOException $e) {
    error_log("Check username error: " . $e->getMessage());
    sendError('Failed to check username', 500);
}

?>
