<?php
require_once 'config.php';

// Get JSON input
$json = file_get_contents('php://input');
$data = json_decode($json, true);

$callId = $data['callId'] ?? $_POST['callId'] ?? '';
$action = $data['action'] ?? $_POST['action'] ?? ''; // 'accept' or 'reject'

if (empty($callId) || empty($action)) {
    echo json_encode(['success' => false, 'message' => 'Call ID and action required']);
    exit;
}

try {
    $pdo = getDbConnection();
    
    $status = ($action === 'accept') ? 'accepted' : 'rejected';
    
    $stmt = $pdo->prepare("UPDATE calls SET status = ? WHERE id = ?");
    
    if ($stmt->execute([$status, $callId])) {
        echo json_encode(['success' => true, 'message' => "Call $status"]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to update call status']);
    }
    
} catch (PDOException $e) {
    error_log("Respond to call error: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Database error']);
}
?>
