<?php
/**
 * Send Follow Request API
 * 
 * POST /send_follow_request.php
 * Body: {userId, targetUserId}
 * Sends a follow request (creates pending relationship)
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

$data = validatePostParams(['userId', 'targetUserId']);

$userId = (int)$data['userId'];
$targetUserId = (int)$data['targetUserId'];

if ($userId === $targetUserId) {
    sendError('Cannot follow yourself');
}

try {
    $pdo = getDbConnection();
    $timestamp = getCurrentTimestamp();
    
    // Check if already following or request pending
    $stmt = $pdo->prepare("SELECT status FROM following WHERE user_id = ? AND following_id = ?");
    $stmt->execute([$userId, $targetUserId]);
    $existing = $stmt->fetch();
    
    if ($existing) {
        if ($existing['status'] === 'pending') {
            sendError('Follow request already sent');
        } else {
            sendError('Already following this user');
        }
    }
    
    // Insert pending follow request in following table
    $stmt = $pdo->prepare("INSERT INTO following (user_id, following_id, status, timestamp) VALUES (?, ?, 'pending', ?)");
    $stmt->execute([$userId, $targetUserId, $timestamp]);
    
    // Insert pending in followers table (mirror)
    $stmt = $pdo->prepare("INSERT INTO followers (user_id, follower_id, status, timestamp) VALUES (?, ?, 'pending', ?)");
    $stmt->execute([$targetUserId, $userId, $timestamp]);
    
    // Create notification for target user
    $stmt = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, timestamp) VALUES (?, ?, 'follow_request', ?)");
    $stmt->execute([$targetUserId, $userId, $timestamp]);
    
    // Check if there's a follow_back notification (meaning target user followed us first)
    // If so, delete it since we're now following them back
    $stmt = $pdo->prepare("DELETE FROM notifications WHERE user_id = ? AND sender_id = ? AND type = 'follow_back'");
    $stmt->execute([$userId, $targetUserId]);
    
    sendSuccess('Follow request sent');
    
} catch (PDOException $e) {
    error_log("Follow request error: " . $e->getMessage());
    sendError('Failed to send follow request', 500);
}
