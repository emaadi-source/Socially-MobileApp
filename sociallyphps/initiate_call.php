<?php
require_once 'config.php';

// Get JSON input
$json = file_get_contents('php://input');
$data = json_decode($json, true);

// Try both POST and JSON input
$callerId = $data['callerId'] ?? $_POST['callerId'] ?? '';
$receiverId = $data['receiverId'] ?? $_POST['receiverId'] ?? '';
$callType = $data['callType'] ?? $_POST['callType'] ?? '';
$channelName = $data['channelName'] ?? $_POST['channelName'] ?? '';

if (empty($callerId) || empty($receiverId) || empty($callType) || empty($channelName)) {
    echo json_encode(['success' => false, 'message' => 'Missing parameters']);
    exit;
}

try {
    $pdo = getDbConnection();
    
    // Insert call record
    $stmt = $pdo->prepare("INSERT INTO calls (caller_id, receiver_id, channel_name, type, status) VALUES (?, ?, ?, ?, 'pending')");
    $stmt->execute([$callerId, $receiverId, $channelName, $callType]);
    
    echo json_encode(['success' => true, 'message' => 'Call initiated', 'callId' => $pdo->lastInsertId()]);
    
} catch (PDOException $e) {
    error_log("Database error in initiate_call.php: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
