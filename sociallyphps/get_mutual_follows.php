<?php
require_once 'config.php';

$userId = $_GET['userId'] ?? '';

if (empty($userId)) {
    echo json_encode(['success' => false, 'message' => 'User ID required']);
    exit;
}

try {
    $pdo = getDbConnection();

    // Get users who I follow AND who follow me back
    $sql = "SELECT u.id, u.username, u.profile_pic_base64 
            FROM users u
            JOIN following f1 ON u.id = f1.following_id
            JOIN following f2 ON u.id = f2.user_id
            WHERE f1.user_id = ? AND f2.following_id = ?";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([$userId, $userId]);
    $users = $stmt->fetchAll();

    $formattedUsers = [];
    foreach ($users as $user) {
        $formattedUsers[] = [
            'id' => $user['id'],
            'username' => $user['username'],
            'profilePicBase64' => $user['profile_pic_base64']
        ];
    }

    echo json_encode(['success' => true, 'users' => $formattedUsers]);

} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
