# Social Media App - PHP API Documentation

## Overview

This directory contains all PHP API endpoints for the social media application. The APIs provide RESTful services for user authentication, posts, stories, comments, likes, follows, and messaging.

## Base URL

```
http://YOUR_IP/sociallyphps/
```

Replace `YOUR_IP` with your server's IP address.

## Setup

1. Copy all PHP files to `C:\xampp\htdocs\sociallyphps\`
2. Ensure MySQL database is set up (see MYSQL_SETUP_GUIDE.md)
3. Update database credentials in `config.php` if needed
4. Test by accessing: `http://localhost/sociallyphps/config.php`

## API Endpoints

### Authentication

#### Register
- **File**: `register.php`
- **Method**: POST
- **Parameters**:
  - `email` (string, required)
  - `password` (string, required, min 6 chars)
  - `username` (string, required, no spaces)
  - `firstName` (string, required)
  - `lastName` (string, required)
  - `dob` (string, required, format: DD-MM-YYYY)
  - `profilePicBase64` (string, required)
  - `deviceId` (string, required)
- **Response**: `{ success, message, userId, user }`

#### Login
- **File**: `login.php`
- **Method**: POST
- **Parameters**:
  - `email` OR `username` (string, required)
  - `password` (string, required)
- **Response**: `{ success, message, user }`

#### Logout
- **File**: `logout.php`
- **Method**: POST
- **Parameters**:
  - `userId` (int, required)
- **Response**: `{ success, message }`

### User Management

#### Get User
- **File**: `get_user.php`
- **Method**: GET
- **Parameters**:
  - `email` OR `username` OR `userId` (required)
- **Response**: `{ success, message, user }`

### Posts

#### Create Post
- **File**: `create_post.php`
- **Method**: POST
- **Parameters**:
  - `userId` (int, required)
  - `mediaBase64` (string, required)
  - `mediaType` (string, required)
  - `caption` (string, required)
- **Response**: `{ success, message, postId, timestamp }`

#### Get Posts
- **File**: `get_posts.php`
- **Method**: GET
- **Parameters**:
  - `userId` (int, optional - for feed)
  - `targetUserId` (int, optional - for specific user's posts)
  - `limit` (int, optional, default: 50)
  - `offset` (int, optional, default: 0)
- **Response**: `{ success, message, posts[], count }`

### Likes

#### Toggle Like
- **File**: `toggle_like.php`
- **Method**: POST
- **Parameters**:
  - `postId` (string, required)
  - `userId` (int, required)
- **Response**: `{ success, message, liked, likeCount }`

### Comments

#### Add Comment
- **File**: `add_comment.php`
- **Method**: POST
- **Parameters**:
  - `postId` (string, required)
  - `userId` (int, required)
  - `text` (string, required)
- **Response**: `{ success, message, commentId, timestamp }`

#### Get Comments
- **File**: `get_comments.php`
- **Method**: GET
- **Parameters**:
  - `postId` (string, required)
- **Response**: `{ success, message, comments[], count }`

### Stories

#### Create Story
- **File**: `create_story.php`
- **Method**: POST
- **Parameters**:
  - `userId` (int, required)
  - `mediaType` (string, required)
  - `chunkCount` (int, required)
  - `chunks[]` (array, required)
- **Response**: `{ success, message, storyId, timestamp }`

#### Get Stories
- **File**: `get_stories.php`
- **Method**: GET
- **Parameters**:
  - `userId` (int, required)
- **Response**: `{ success, message, stories[], count }`
- **Note**: Returns stories from last 24 hours only

### Follow/Unfollow

#### Follow User
- **File**: `follow_user.php`
- **Method**: POST
- **Parameters**:
  - `userId` (int, required)
  - `targetUserId` (int, required)
- **Response**: `{ success, message }`

#### Unfollow User
- **File**: `unfollow_user.php`
- **Method**: POST
- **Parameters**:
  - `userId` (int, required)
  - `targetUserId` (int, required)
- **Response**: `{ success, message }`

#### Check Following
- **File**: `check_following.php`
- **Method**: GET
- **Parameters**:
  - `userId` (int, required)
  - `targetUserId` (int, required)
- **Response**: `{ success, message, isFollowing }`

### Sessions

#### Create Session
- **File**: `create_session.php`
- **Method**: POST
- **Parameters**:
  - `userId` (int, required)
  - `deviceId` (string, required)
- **Response**: `{ success, message, sessionId }`

## Additional APIs Needed

The following APIs are referenced in the implementation plan but not yet created. You can create them following the same pattern:

1. **update_user.php** - Update user profile
2. **check_username.php** - Check username availability
3. **get_post.php** - Get single post details
4. **delete_post.php** - Delete a post
5. **get_user_stories.php** - Get stories for specific user
6. **get_followers.php** - Get user's followers list
7. **get_following.php** - Get users that a user follows
8. **update_session.php** - Update session active status
9. **get_active_sessions.php** - Get active sessions for user
10. **send_message.php** - Send direct message
11. **get_messages.php** - Get messages between users
12. **get_conversations.php** - Get all conversations
13. **update_fcm_token.php** - Update FCM token
14. **check_like.php** - Check if user liked a post

## Error Handling

All APIs return JSON responses with the following structure:

**Success**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

**Error**:
```json
{
  "success": false,
  "message": "Error description"
}
```

## HTTP Status Codes

- `200` - Success
- `400` - Bad Request (missing/invalid parameters)
- `401` - Unauthorized (invalid credentials)
- `404` - Not Found (resource doesn't exist)
- `409` - Conflict (duplicate entry)
- `500` - Internal Server Error

## Testing APIs

### Using Postman

1. Install Postman
2. Create a new request
3. Set method (GET/POST)
4. Set URL: `http://localhost/sociallyphps/[endpoint].php`
5. For POST requests:
   - Go to Body tab
   - Select "raw" and "JSON"
   - Enter JSON data
6. Send request

### Using curl (Command Line)

**Register**:
```bash
curl -X POST http://localhost/sociallyphps/register.php \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","username":"testuser","firstName":"Test","lastName":"User","dob":"01-01-2000","profilePicBase64":"base64string","deviceId":"device123"}'
```

**Login**:
```bash
curl -X POST http://localhost/sociallyphps/login.php \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

**Get User**:
```bash
curl "http://localhost/sociallyphps/get_user.php?username=testuser"
```

## Security Notes

**For Production**:

1. **Enable HTTPS** - Never use HTTP in production
2. **Update CORS** - Restrict `Access-Control-Allow-Origin` to your app's domain
3. **Add Authentication Tokens** - Implement JWT or session tokens
4. **Rate Limiting** - Add rate limiting to prevent abuse
5. **Input Validation** - Already implemented, but review for your use case
6. **SQL Injection Protection** - Using PDO prepared statements (already implemented)
7. **Password Security** - Using `password_hash()` (already implemented)
8. **Error Logging** - Configure proper error logging (don't display errors to users)
9. **Database Credentials** - Store in environment variables, not in code
10. **File Permissions** - Set proper permissions on PHP files (644)

## Troubleshooting

### "Database connection failed"
- Check if MySQL is running in XAMPP
- Verify database credentials in `config.php`
- Ensure database `sociallypersonal` exists

### "404 Not Found"
- Verify files are in `C:\xampp\htdocs\sociallyphps\`
- Check file names match exactly (case-sensitive on some systems)
- Ensure Apache is running in XAMPP

### "CORS error" from Android app
- CORS headers are already set in `config.php`
- If still having issues, check Android app network security config

### "Missing required parameters"
- Check request body format (must be valid JSON for POST)
- Verify parameter names match exactly (case-sensitive)

### "Invalid credentials"
- Password is case-sensitive
- Check if user exists in database
- Verify password was hashed correctly during registration

## File Structure

```
sociallyphps/
├── config.php              # Database configuration
├── utils.php               # Utility functions
├── register.php            # User registration
├── login.php               # User login
├── logout.php              # User logout
├── get_user.php            # Get user profile
├── create_post.php         # Create new post
├── get_posts.php           # Get posts/feed
├── toggle_like.php         # Like/unlike post
├── add_comment.php         # Add comment
├── get_comments.php        # Get comments
├── create_story.php        # Create story
├── get_stories.php         # Get stories
├── follow_user.php         # Follow user
├── unfollow_user.php       # Unfollow user
├── check_following.php     # Check follow status
├── create_session.php      # Create session
└── logs/                   # API request logs (auto-created)
```

## Next Steps

1. ✅ PHP APIs created
2. ⏭️ Create remaining APIs (see "Additional APIs Needed" section)
3. ⏭️ Test all APIs with Postman
4. ⏭️ Create Android app components
5. ⏭️ Integrate Android app with APIs
6. ⏭️ Test offline functionality

## Support

For issues or questions:
- Check XAMPP error logs: `C:\xampp\apache\logs\error.log`
- Check PHP error logs: `C:\xampp\php\logs\php_error_log`
- Check API request logs: `sociallyphps/logs/api_requests.log`

---

**Version**: 1.0  
**Last Updated**: 2025-11-25
