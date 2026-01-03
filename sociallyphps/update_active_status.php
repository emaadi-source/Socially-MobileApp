<?php
require_once 'config.php';

// Get JSON input
$json = file_get_contents('php://input');
$data = json_decode($json, true);

$userId = $data['userId'] ?? $_POST['userId'] ?? $_GET['userId'] ?? '';

if (empty($userId)) {
    echo json_encode(['success' => false, 'message' => 'User ID required']);
    exit;
}

try {
    $pdo = getDbConnection();
    $timestamp = round(microtime(true) * 1000);
    
    $stmt = $pdo->prepare("UPDATE users SET last_active = ? WHERE id = ?");
    
    if ($stmt->execute([$timestamp, $userId])) {
        echo json_encode(['success' => true, 'message' => 'Status updated', 'timestamp' => $timestamp]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to update status']);
    }
} catch (PDOException $e) {
    error_log("Update active status error: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
