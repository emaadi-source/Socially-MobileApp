<?php
require_once 'config.php';

// Agora credentials
$appId = "ded3a611a88e46f2ae8c24426cdf467c";
$appCertificate = "61e1a4d5c7b04642a0d6a64994428ec5";

$channelName = $_GET['channelName'] ?? '';
$uid = intval($_GET['uid'] ?? 0);

if (empty($channelName)) {
    echo json_encode(['success' => false, 'message' => 'Channel name required']);
    exit;
}

// Token generation using Agora algorithm
function generateRtcToken($appId, $appCertificate, $channelName, $uid, $role, $expireTime) {
    $version = "006";
    $randomInt = rand();
    $timestamp = time();
    $expiredTs = $timestamp + $expireTime;
    
    $rawMessage = $appId . $channelName . pack("V", $uid) . pack("V", $timestamp) . pack("V", $randomInt) . pack("V", $expiredTs);
    $signature = hash_hmac('sha256', $rawMessage, $appCertificate, true);
    
    $content = pack("v", strlen($signature)) . $signature . 
               pack("V", $timestamp) . 
               pack("V", $randomInt) . 
               pack("V", $expiredTs);
    
    $token = $version . base64_encode($content);
    return $token;
}

try {
    $role = 1; // Publisher role
    $expireTime = 3600; // 1 hour
    
    $token = generateRtcToken($appId, $appCertificate, $channelName, $uid, $role, $expireTime);
    
    echo json_encode([
        'success' => true,
        'token' => $token,
        'uid' => $uid,
        'expireTime' => $expireTime
    ]);
    
} catch (Exception $e) {
    error_log("Token generation error: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'Token generation failed: ' . $e->getMessage()
    ]);
}
?>
