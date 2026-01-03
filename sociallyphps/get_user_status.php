<?php
require_once 'config.php';

$userId = $_GET['userId'] ?? '';

if (empty($userId)) {
    echo json_encode(['success' => false, 'message' => 'User ID required', 'isOnline' => false]);
    exit;
}

try {
    $pdo = getDbConnection();
    
    // Check if user has any active sessions updated in last 30 seconds
    $stmt = $pdo->prepare("SELECT updated_at FROM sessions WHERE user_id = ? AND is_active = 1 ORDER BY updated_at DESC LIMIT 1");
    $stmt->execute([$userId]);
    $session = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$session) {
        echo json_encode(['success' => true, 'isOnline' => false, 'lastActive' => 0]);
        exit;
    }
    
    $lastActive = (int)$session['updated_at'];
    $currentTime = round(microtime(true) * 1000);
    
    // User is online if session updated in last 30 seconds
    $isOnline = ($currentTime - $lastActive) < 30000;
    
    echo json_encode([
        'success' => true,
        'isOnline' => $isOnline,
        'lastActive' => $lastActive
    ]);
    
} catch (PDOException $e) {
    error_log("Get user status error: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Database error', 'isOnline' => false]);
}
?>
