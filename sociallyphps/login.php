<?php
/**
 * User Login API
 * 
 * POST /login.php
 * Authenticates user and returns user data
 */

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

// Validate required parameters (either email or username, plus password)
$data = json_decode(file_get_contents('php://input'), true);

if ($data === null) {
    $data = $_POST;
}

if (!isset($data['password']) || trim($data['password']) === '') {
    sendError('Password is required');
}

$password = $data['password'];
$email = isset($data['email']) ? sanitizeString($data['email']) : null;
$username = isset($data['username']) ? sanitizeString($data['username']) : null;

if (!$email && !$username) {
    sendError('Either email or username is required');
}

try {
    $pdo = getDbConnection();
    
    // Get user by email or username
    if ($email) {
        $user = getUserByEmail($pdo, $email, true);
    } else {
        $user = getUserByUsername($pdo, $username, true);
    }
    
    if (!$user) {
        sendError('Invalid credentials', 401);
    }
    
    // Verify password
    if (!verifyPassword($password, $user['password_hash'])) {
        sendError('Invalid credentials', 401);
    }
    
    // Update last login and login status
    $timestamp = getCurrentTimestamp();
    $stmt = $pdo->prepare("
        UPDATE users 
        SET last_login = ?, is_logged_in = ? 
        WHERE id = ?
    ");
    $stmt->execute([$timestamp, true, $user['id']]);
    
    // Create or update session
    $deviceId = isset($data['deviceId']) ? sanitizeString($data['deviceId']) : null;
    if ($deviceId) {
        // Check if session exists for this device
        $stmt = $pdo->prepare("SELECT id FROM sessions WHERE user_id = ? AND device_id = ?");
        $stmt->execute([$user['id'], $deviceId]);
        $existingSession = $stmt->fetch();
        
        if ($existingSession) {
            // Update existing session
            $stmt = $pdo->prepare("
                UPDATE sessions 
                SET is_active = ?, is_logged_in = ?, updated_at = ?, last_active = ?
                WHERE user_id = ? AND device_id = ?
            ");
            $stmt->execute([true, true, $timestamp, $timestamp, $user['id'], $deviceId]);
        } else {
            // Create new session
            $sessionId = generateUniqueId();
            $stmt = $pdo->prepare("
                INSERT INTO sessions (user_id, session_id, device_id, is_active, is_logged_in, created_at, updated_at, last_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ");
            $stmt->execute([$user['id'], $sessionId, $deviceId, true, true, $timestamp, $timestamp, $timestamp]);
        }
    }
    
    // Remove password hash from response
    unset($user['password_hash']);
    
    // Return success with user data
    sendSuccess('Login successful', [
        'user' => [
            'id' => (int)$user['id'],
            'email' => $user['email'],
            'username' => $user['username'],
            'firstName' => $user['first_name'],
            'lastName' => $user['last_name'],
            'dob' => $user['dob'],
            'profilePicBase64' => $user['profile_pic_base64'],
            'deviceId' => $user['device_id'],
            'lastLogin' => (int)$timestamp,
            'isLoggedIn' => true
        ]
    ]);
    
} catch (PDOException $e) {
    error_log("Login error: " . $e->getMessage());
    sendError('Login failed. Please try again later', 500);
}

?>
