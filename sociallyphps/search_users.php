<?php
/**
 * Search Users API
 * 
 * GET /search_users.php?query=xxx&userId=xxx
 * Searches users by username or name (excludes current user)
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

$query = isset($_GET['query']) ? sanitizeString($_GET['query']) : '';
$userId = isset($_GET['userId']) ? (int)$_GET['userId'] : null;

if (empty($query)) {
    sendSuccess('No results', ['data' => ['users' => []]]);
}

if (!$userId) {
    sendError('userId is required');
}

try {
    $pdo = getDbConnection();
    
    // Search by username or name, exclude current user
    $searchTerm = "%{$query}%";
    $stmt = $pdo->prepare("
        SELECT id, username, first_name, last_name, profile_pic_base64
        FROM users 
        WHERE (username LIKE ? OR CONCAT(first_name, ' ', last_name) LIKE ?)
        AND id != ?
        LIMIT 20
    ");
    $stmt->execute([$searchTerm, $searchTerm, $userId]);
    $users = $stmt->fetchAll();
    
    $results = [];
    foreach ($users as $user) {
        $results[] = [
            'id' => (int)$user['id'],
            'username' => $user['username'],
            'firstName' => $user['first_name'],
            'lastName' => $user['last_name'],
            'profilePicBase64' => $user['profile_pic_base64']
        ];
    }
    
    sendSuccess('Search results', [
        'data' => [
            'users' => $results,
            'count' => count($results)
        ]
    ]);
    
} catch (PDOException $e) {
    error_log("Search error: " . $e->getMessage());
    sendError('Search failed', 500);
}
