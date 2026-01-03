<?php
/**
 * Utility Functions for API
 * 
 * This file contains helper functions used across all API endpoints
 */

/**
 * Send JSON response
 * 
 * @param array $data Response data
 * @param int $statusCode HTTP status code
 */
function sendResponse($data, $statusCode = 200) {
    http_response_code($statusCode);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit();
}

/**
 * Send success response
 * 
 * @param string $message Success message
 * @param array $data Additional data
 */
function sendSuccess($message, $data = []) {
    $response = array_merge(['success' => true, 'message' => $message], $data);
    sendResponse($response, 200);
}

/**
 * Send error response
 * 
 * @param string $message Error message
 * @param int $statusCode HTTP status code
 * @param array $data Additional error data
 */
function sendError($message, $statusCode = 400, $data = []) {
    $response = array_merge(['success' => false, 'message' => $message], $data);
    sendResponse($response, $statusCode);
}

/**
 * Validate required POST parameters
 * 
 * @param array $required Array of required parameter names
 * @return array Validated parameters
 */
function validatePostParams($required) {
    $data = json_decode(file_get_contents('php://input'), true);
    
    if ($data === null) {
        // Try regular POST if JSON decode fails
        $data = $_POST;
    }
    
    $missing = [];
    foreach ($required as $param) {
        if (!isset($data[$param]) || trim($data[$param]) === '') {
            $missing[] = $param;
        }
    }
    
    if (!empty($missing)) {
        sendError('Missing required parameters: ' . implode(', ', $missing), 400);
    }
    
    return $data;
}

/**
 * Validate required GET parameters
 * 
 * @param array $required Array of required parameter names
 * @return array Validated parameters
 */
function validateGetParams($required) {
    $missing = [];
    foreach ($required as $param) {
        if (!isset($_GET[$param]) || trim($_GET[$param]) === '') {
            $missing[] = $param;
        }
    }
    
    if (!empty($missing)) {
        sendError('Missing required parameters: ' . implode(', ', $missing), 400);
    }
    
    return $_GET;
}

/**
 * Validate email format
 * 
 * @param string $email Email address
 * @return bool True if valid
 */
function validateEmail($email) {
    return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
}

/**
 * Sanitize string input
 * 
 * @param string $input Input string
 * @return string Sanitized string
 */
function sanitizeString($input) {
    return htmlspecialchars(strip_tags(trim($input)), ENT_QUOTES, 'UTF-8');
}

/**
 * Hash password
 * 
 * @param string $password Plain text password
 * @return string Hashed password
 */
function hashPassword($password) {
    return password_hash($password, PASSWORD_DEFAULT);
}

/**
 * Verify password
 * 
 * @param string $password Plain text password
 * @param string $hash Hashed password
 * @return bool True if password matches
 */
function verifyPassword($password, $hash) {
    return password_verify($password, $hash);
}

/**
 * Check if request method is allowed
 * 
 * @param string $method Expected method (GET, POST, etc.)
 */
function checkRequestMethod($method) {
    if ($_SERVER['REQUEST_METHOD'] !== $method) {
        sendError('Invalid request method. Expected ' . $method, 405);
    }
}

/**
 * Get user data by ID
 * 
 * @param PDO $pdo Database connection
 * @param int $userId User ID
 * @param bool $includePassword Whether to include password hash
 * @return array|null User data or null if not found
 */
function getUserById($pdo, $userId, $includePassword = false) {
    $fields = $includePassword 
        ? '*' 
        : 'id, email, username, first_name, last_name, dob, profile_pic_base64, device_id, last_login, is_logged_in, created_at';
    
    $stmt = $pdo->prepare("SELECT $fields FROM users WHERE id = ?");
    $stmt->execute([$userId]);
    return $stmt->fetch();
}

/**
 * Get user data by email
 * 
 * @param PDO $pdo Database connection
 * @param string $email User email
 * @param bool $includePassword Whether to include password hash
 * @return array|null User data or null if not found
 */
function getUserByEmail($pdo, $email, $includePassword = false) {
    $fields = $includePassword 
        ? '*' 
        : 'id, email, username, first_name, last_name, dob, profile_pic_base64, device_id, last_login, is_logged_in, created_at';
    
    $stmt = $pdo->prepare("SELECT $fields FROM users WHERE email = ?");
    $stmt->execute([$email]);
    return $stmt->fetch();
}

/**
 * Get user data by username
 * 
 * @param PDO $pdo Database connection
 * @param string $username Username
 * @param bool $includePassword Whether to include password hash
 * @return array|null User data or null if not found
 */
function getUserByUsername($pdo, $username, $includePassword = false) {
    $fields = $includePassword 
        ? '*' 
        : 'id, email, username, first_name, last_name, dob, profile_pic_base64, device_id, last_login, is_logged_in, created_at';
    
    $stmt = $pdo->prepare("SELECT $fields FROM users WHERE username = ?");
    $stmt->execute([$username]);
    return $stmt->fetch();
}

/**
 * Log API request (for debugging)
 * 
 * @param string $endpoint Endpoint name
 * @param array $data Request data
 */
function logRequest($endpoint, $data = []) {
    $logFile = __DIR__ . '/logs/api_requests.log';
    $logDir = dirname($logFile);
    
    if (!is_dir($logDir)) {
        mkdir($logDir, 0755, true);
    }
    
    $logEntry = [
        'timestamp' => date('Y-m-d H:i:s'),
        'endpoint' => $endpoint,
        'method' => $_SERVER['REQUEST_METHOD'],
        'ip' => $_SERVER['REMOTE_ADDR'] ?? 'unknown',
        'data' => $data
    ];
    
    file_put_contents($logFile, json_encode($logEntry) . PHP_EOL, FILE_APPEND);
}

/**
 * Validate date format (DD-MM-YYYY)
 * 
 * @param string $date Date string
 * @return bool True if valid
 */
function validateDateFormat($date) {
    $d = DateTime::createFromFormat('d-m-Y', $date);
    return $d && $d->format('d-m-Y') === $date;
}

/**
 * Convert date from DD-MM-YYYY to YYYY-MM-DD for MySQL
 * 
 * @param string $date Date in DD-MM-YYYY format
 * @return string Date in YYYY-MM-DD format
 */
function convertDateForMySQL($date) {
    $d = DateTime::createFromFormat('d-m-Y', $date);
    return $d ? $d->format('Y-m-d') : null;
}

/**
 * Check if user is 18 or older
 * 
 * @param string $dob Date of birth in YYYY-MM-DD format
 * @return bool True if 18 or older
 */
function isUserAdult($dob) {
    $birthDate = new DateTime($dob);
    $today = new DateTime();
    $age = $today->diff($birthDate)->y;
    return $age >= 18;
}

/**
 * Generate session ID
 * 
 * @return string Unique session ID
 */
function generateSessionId() {
    return 'session_' . uniqid() . '_' . bin2hex(random_bytes(16));
}

/**
 * Generate unique ID (for posts, stories, etc.)
 * 
 * @return string Unique ID
 */
function generateUniqueId() {
    return uniqid() . '_' . bin2hex(random_bytes(8));
}

/**
 * Get current timestamp in milliseconds
 * 
 * @return int Timestamp
 */
/**
 * Get current timestamp in milliseconds
 * 
 * @return int Timestamp
 */
function getCurrentTimestamp() {
    return round(microtime(true) * 1000);
}

/**
 * Send FCM Notification
 * 
 * @param string $token FCM Token
 * @param array $data Data payload
 * @param array $notification Notification payload (optional)
 * @return bool|string Response or false on failure
 */
function sendFCMNotification($token, $data = [], $notification = null) {
    $url = 'https://fcm.googleapis.com/fcm/send';
    
    $fields = [
        'to' => $token,
        'data' => $data,
        'priority' => 'high'
    ];
    
    if ($notification) {
        $fields['notification'] = $notification;
    }
    
    $headers = [
        'Authorization: key=' . FCM_SERVER_KEY,
        'Content-Type: application/json'
    ];
    
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
    
    $result = curl_exec($ch);
    
    if ($result === FALSE) {
        error_log('FCM Send Error: ' . curl_error($ch));
        curl_close($ch);
        return false;
    }
    
    curl_close($ch);
    return $result;
}
