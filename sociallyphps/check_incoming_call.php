<?php
require_once 'config.php';

$userId = $_GET['userId'] ?? '';

if (empty($userId)) {
    echo json_encode(['success' => false, 'message' => 'User ID required']);
    exit;
}

try {
    $pdo = getDbConnection();
    
    // Get pending calls for this user
    $stmt = $pdo->prepare("SELECT c.*, u.username as caller_name, u.profile_pic_base64 as caller_pic 
                           FROM calls c 
                           JOIN users u ON c.caller_id = u.id 
                           WHERE c.receiver_id = ? AND c.status = 'pending' 
                           ORDER BY c.created_at DESC 
                           LIMIT 1");
    $stmt->execute([$userId]);
    $call = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($call) {
        echo json_encode([
            'success' => true,
            'hasCall' => true,
            'call' => [
                'id' => $call['id'],
                'callerId' => $call['caller_id'],
                'callerName' => $call['caller_name'],
                'callerPic' => $call['caller_pic'],
                'channelName' => $call['channel_name'],
                'type' => $call['type']
            ]
        ]);
    } else {
        echo json_encode(['success' => true, 'hasCall' => false]);
    }
    
} catch (PDOException $e) {
    error_log("Check incoming call error: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Database error']);
}
?>
