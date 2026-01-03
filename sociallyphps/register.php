<?php
/**
 * User Registration API
 * 
 * POST /register.php
 * Creates a new user account
 */

// Add CORS headers
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');
header('Content-Type: application/json');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'config.php';
require_once 'utils.php';

checkRequestMethod('POST');

// Validate required parameters
$data = validatePostParams([
    'email',
    'password',
    'username',
    'firstName',
    'lastName',
    'dob',
    'profilePicBase64',
    'deviceId'
]);

// Sanitize inputs
$email = sanitizeString($data['email']);
$password = $data['password'];
$username = sanitizeString($data['username']);
$firstName = sanitizeString($data['firstName']);
$lastName = sanitizeString($data['lastName']);
$dob = sanitizeString($data['dob']);
$profilePicBase64 = $data['profilePicBase64'];
$deviceId = sanitizeString($data['deviceId']);

// Validate email format
if (!validateEmail($email)) {
    sendError('Invalid email format');
}

// Validate username (no spaces)
if (strpos($username, ' ') !== false) {
    sendError('Username cannot contain spaces');
}

// Validate date format and convert
if (!validateDateFormat($dob)) {
    sendError('Invalid date format. Use DD-MM-YYYY');
}

$dobMySQL = convertDateForMySQL($dob);

// Check if user is 18 or older
if (!isUserAdult($dobMySQL)) {
    sendError('You must be at least 18 years old to create an account');
}

// Validate password strength (minimum 6 characters)
if (strlen($password) < 6) {
    sendError('Password must be at least 6 characters long');
}

try {
    $pdo = getDbConnection();
    
    // Check if email already exists
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        sendError('Email already registered', 409);
    }
    
    // Check if username already exists
    $stmt = $pdo->prepare("SELECT id FROM users WHERE username = ?");
    $stmt->execute([$username]);
    if ($stmt->fetch()) {
        sendError('Username already taken', 409);
    }
    
    // Hash password
    $passwordHash = hashPassword($password);
    
    // Get current timestamp
    $timestamp = getCurrentTimestamp();
    
    // Insert new user
    $stmt = $pdo->prepare("
        INSERT INTO users (
            email, username, password_hash, first_name, last_name, 
            dob, profile_pic_base64, device_id, last_login, is_logged_in
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ");
    
    $stmt->execute([
        $email,
        $username,
        $passwordHash,
        $firstName,
        $lastName,
        $dobMySQL,
        $profilePicBase64,
        $deviceId,
        $timestamp,
        true
    ]);
    
    $userId = $pdo->lastInsertId();
    
    // Return success with user data
    sendSuccess('Account created successfully', [
        'userId' => (int)$userId,
        'user' => [
            'id' => (int)$userId,
            'email' => $email,
            'username' => $username,
            'firstName' => $firstName,
            'lastName' => $lastName,
            'profilePicBase64' => $profilePicBase64
        ]
    ]);
    
} catch (PDOException $e) {
    error_log("Registration error: " . $e->getMessage());
    sendError('Registration failed. Please try again later', 500);
}

?>
