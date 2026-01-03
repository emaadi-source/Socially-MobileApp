package com.faujipanda.i230665_i230026

/**
 * API Configuration
 * Contains all API endpoint URLs and configuration constants
 */
object ApiConfig {
    // Base configuration - PUBLIC so it can be accessed for error messages
    const val BASE_IP = "10.38.229.102" // Change this to your server IP
    
    const val BASE_URL = "http://$BASE_IP/sociallyphps/"
    
    // Authentication endpoints
    const val REGISTER = "${BASE_URL}register.php"
    const val LOGIN = "${BASE_URL}login.php"
    const val LOGOUT = "${BASE_URL}logout.php"
    
    // User management endpoints
    const val GET_USER = "${BASE_URL}get_user.php"
    const val UPDATE_USER = "${BASE_URL}update_user.php"
    const val CHECK_USERNAME = "${BASE_URL}check_username.php"
    const val SEARCH_USERS = "${BASE_URL}search_users.php"
    
    // Post management endpoints
    const val CREATE_POST = "${BASE_URL}create_post.php"
    const val GET_POSTS = "${BASE_URL}get_posts.php"
    const val GET_POST = "${BASE_URL}get_post.php"
    const val DELETE_POST = "${BASE_URL}delete_post.php"
    
    // Story management endpoints
    const val CREATE_STORY = "${BASE_URL}add_story.php"
    const val GET_STORIES = "${BASE_URL}get_stories.php"
    const val GET_USER_STORIES = "${BASE_URL}get_user_stories.php"
    
    // Comment management endpoints
    const val ADD_COMMENT = "${BASE_URL}add_comment.php"
    const val GET_COMMENTS = "${BASE_URL}get_comments.php"
    
    // Like management endpoints
    const val TOGGLE_LIKE = "${BASE_URL}toggle_like.php"
    const val CHECK_LIKE = "${BASE_URL}check_like.php"
    
    // Follow/Follower endpoints
    const val FOLLOW_USER = "${BASE_URL}follow_user.php"
    const val UNFOLLOW_USER = "${BASE_URL}unfollow_user.php"
    const val GET_FOLLOWERS = "${BASE_URL}get_followers.php"
    const val GET_FOLLOWING = "${BASE_URL}get_following.php"
    const val CHECK_FOLLOWING = "${BASE_URL}check_following.php"
    const val RESPOND_FOLLOW_REQUEST = "${BASE_URL}respond_follow_request.php"
    
    // Session management endpoints
    const val CREATE_SESSION = "${BASE_URL}create_session.php"
    const val UPDATE_SESSION = "${BASE_URL}update_session.php"
    const val GET_ACTIVE_SESSIONS = "${BASE_URL}get_active_sessions.php"
    
    // Messaging endpoints
    const val SEND_MESSAGE = "${BASE_URL}send_message.php"
    const val GET_MESSAGES = "${BASE_URL}get_messages.php"
    const val GET_USER_STATUS = "${BASE_URL}get_user_status.php"
    const val INITIATE_CALL = "${BASE_URL}initiate_call.php"
    const val CHECK_INCOMING_CALL = "${BASE_URL}check_incoming_call.php"
    const val RESPOND_TO_CALL = "${BASE_URL}respond_to_call.php"
    const val GENERATE_AGORA_TOKEN = "${BASE_URL}generate_agora_token.php"
    const val GET_MUTUAL_FOLLOWS = "${BASE_URL}get_mutual_follows.php"
    const val EDIT_MESSAGE = "${BASE_URL}edit_message.php"
    const val DELETE_MESSAGE = "${BASE_URL}delete_message.php"
    // Notifications endpoints
    const val GET_NOTIFICATIONS = "${BASE_URL}get_notifications.php"
    const val MARK_NOTIFICATION_READ = "${BASE_URL}mark_notification_read.php"
    
    // Feed & Session endpoints
    const val GET_FEED = "${BASE_URL}get_feed.php"
    const val CHECK_SESSION_STATUS = "${BASE_URL}check_session_status.php"
    const val UPDATE_ACTIVE_STATUS = "${BASE_URL}update_active_status.php"
    
    // Request timeout configurations (in milliseconds)
    const val CONNECTION_TIMEOUT = 30000 // 30 seconds
    const val READ_TIMEOUT = 30000 // 30 seconds
    
    // Retry configuration
    const val MAX_RETRIES = 3
    const val RETRY_DELAY = 1000L // 1 second
}
