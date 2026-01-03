<?php
/**
 * Mark Notification as Read API
 * 
 * POST /mark_notification_read.php
 * Body: {notificationId}
 * Marks a notification as read
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

checkRequestMethod('POST');

$data = validatePostParams(['notificationId']);

$notificationId = (int)$data['notificationId'];

try {
    $pdo = getDbConnection();
    
    // Mark notification as read
    $stmt = $pdo->prepare("UPDATE notifications SET is_read = 1 WHERE id = ?");
    $stmt->execute([$notificationId]);
    
    sendSuccess('Notification marked as read');
    
} catch (PDOException $e) {
    error_log("Mark notification read error: " . $e->getMessage());
    sendError('Failed to mark notification as read', 500);
}
