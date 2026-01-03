package com.faujipanda.i230665_i230026

import android.content.Context
import android.util.Log
import org.json.JSONObject

/**
 * Offline Repository
 * Provides unified interface for data operations with offline-first approach
 */
class OfflineRepository(private val context: Context) {

    private val apiClient = ApiClient(context)
    private val dbHelper = LocalDatabaseHelper(context)
    private val syncManager = SyncManager(context)

    companion object {
        private const val TAG = "OfflineRepository"
    }

    // ========== POST OPERATIONS ==========

    /**
     * Create post (offline-first)
     * Saves locally and queues for sync
     */
    fun createPost(
        userId: Int,
        mediaBase64: String,
        mediaType: String,
        caption: String,
        onComplete: (success: Boolean, postId: String?, message: String) -> Unit
    ) {
        // Save to local database first
        val localId = dbHelper.insertPost(userId, mediaBase64, mediaType, caption)
        
        if (localId == -1L) {
            onComplete(false, null, "Failed to save post locally")
            return
        }

        Log.d(TAG, "Post saved locally with ID: $localId")

        // If online, sync immediately
        if (apiClient.isNetworkAvailable()) {
            val params = mapOf(
                "userId" to userId,
                "mediaBase64" to mediaBase64,
                "mediaType" to mediaType,
                "caption" to caption
            )

            apiClient.post(
                ApiConfig.CREATE_POST,
                params,
                onSuccess = { response ->
                    val success = response.optBoolean("success", false)
                    if (success) {
                        val postId = response.optString("postId")
                        dbHelper.updatePostServerId(localId, postId)
                        onComplete(true, postId, "Post created successfully")
                    } else {
                        // Queue for later sync
                        syncManager.queueForSync("POST", localId, params, ApiConfig.CREATE_POST)
                        onComplete(true, null, "Post saved (pending sync)")
                    }
                },
                onError = { error ->
                    // Queue for later sync
                    syncManager.queueForSync("POST", localId, params, ApiConfig.CREATE_POST)
                    onComplete(true, null, "Post saved (will sync when online)")
                }
            )
        } else {
            // Queue for sync when online
            val params = mapOf(
                "userId" to userId,
                "mediaBase64" to mediaBase64,
                "mediaType" to mediaType,
                "caption" to caption
            )
            syncManager.queueForSync("POST", localId, params, ApiConfig.CREATE_POST)
            onComplete(true, null, "Post saved (will sync when online)")
        }
    }

    // ========== STORY OPERATIONS ==========

    /**
     * Create story (offline-first)
     */
    fun createStory(
        userId: Int,
        mediaBase64: String,
        mediaType: String,
        onComplete: (success: Boolean, storyId: String?, message: String) -> Unit
    ) {
        // Save to local database first
        val localId = dbHelper.insertStory(userId, mediaBase64, mediaType)

        if (localId == -1L) {
            onComplete(false, null, "Failed to save story locally")
            return
        }

        Log.d(TAG, "Story saved locally with ID: $localId")

        // If online, sync immediately
        if (apiClient.isNetworkAvailable()) {
            val params = mapOf(
                "userId" to userId,
                "mediaBase64" to mediaBase64,
                "mediaType" to mediaType
            )

            apiClient.post(
                ApiConfig.CREATE_STORY,
                params,
                onSuccess = { response ->
                    val success = response.optBoolean("success", false)
                    if (success) {
                        val storyId = response.optString("storyId")
                        dbHelper.updateStoryServerId(localId, storyId)
                        onComplete(true, storyId, "Story uploaded successfully")
                    } else {
                        // Queue for later sync
                        syncManager.queueForSync("STORY", localId, params, ApiConfig.CREATE_STORY)
                        onComplete(true, null, "Story saved (pending sync)")
                    }
                },
                onError = { error ->
                    // Queue for later sync
                    syncManager.queueForSync("STORY", localId, params, ApiConfig.CREATE_STORY)
                    onComplete(true, null, "Story saved (will sync when online)")
                }
            )
        } else {
            // Queue for sync when online
            val params = mapOf(
                "userId" to userId,
                "mediaBase64" to mediaBase64,
                "mediaType" to mediaType
            )
            syncManager.queueForSync("STORY", localId, params, ApiConfig.CREATE_STORY)
            onComplete(true, null, "Story saved (will sync when online)")
        }
    }

    /**
     * Get posts (cache-first, then fetch)
     */
    fun getPosts(
        userId: Int?,
        targetUserId: Int?,
        onComplete: (posts: List<Map<String, Any>>) -> Unit
    ) {
        // TODO: Load from cache first for instant display
        
        // Then fetch from server if online
        if (apiClient.isNetworkAvailable()) {
            val params = mutableMapOf<String, String>()
            userId?.let { params["userId"] = it.toString() }
            targetUserId?.let { params["targetUserId"] = it.toString() }

            val url = apiClient.buildUrlWithParams(ApiConfig.GET_POSTS, params)

            apiClient.get(
                url,
                onSuccess = { response ->
                    val success = response.optBoolean("success", false)
                    if (success) {
                        val postsArray = response.optJSONArray("posts")
                        val posts = mutableListOf<Map<String, Any>>()
                        
                        postsArray?.let {
                            for (i in 0 until it.length()) {
                                val postObj = it.getJSONObject(i)
                                val post = mutableMapOf<String, Any>()
                                post["postId"] = postObj.optString("postId")
                                post["userId"] = postObj.optInt("userId")
                                post["username"] = postObj.optString("username")
                                post["firstName"] = postObj.optString("firstName")
                                post["lastName"] = postObj.optString("lastName")
                                post["profilePicBase64"] = postObj.optString("profilePicBase64")
                                post["mediaBase64"] = postObj.optString("mediaBase64")
                                post["mediaType"] = postObj.optString("mediaType")
                                post["caption"] = postObj.optString("caption")
                                post["timestamp"] = postObj.optLong("timestamp")
                                post["likes"] = postObj.optInt("likes")
                                post["commentsCount"] = postObj.optInt("commentsCount")
                                post["isLiked"] = postObj.optBoolean("isLiked")
                                posts.add(post)
                            }
                        }
                        
                        // TODO: Update cache
                        onComplete(posts)
                    } else {
                        onComplete(emptyList())
                    }
                },
                onError = { error ->
                    Log.e(TAG, "Failed to fetch posts: $error")
                    // TODO: Return cached posts
                    onComplete(emptyList())
                }
            )
        } else {
            // TODO: Return cached posts
            onComplete(emptyList())
        }
    }

    // ========== LIKE OPERATIONS ==========

    /**
     * Toggle like (offline-first)
     */
    fun toggleLike(
        postId: String,
        userId: Int,
        onComplete: (success: Boolean, liked: Boolean, likeCount: Int) -> Unit
    ) {
        // TODO: Update local database first
        
        if (apiClient.isNetworkAvailable()) {
            val params = mapOf(
                "postId" to postId,
                "userId" to userId
            )

            apiClient.post(
                ApiConfig.TOGGLE_LIKE,
                params,
                onSuccess = { response ->
                    val success = response.optBoolean("success", false)
                    if (success) {
                        val liked = response.optBoolean("liked")
                        val likeCount = response.optInt("likeCount")
                        onComplete(true, liked, likeCount)
                    } else {
                        onComplete(false, false, 0)
                    }
                },
                onError = { error ->
                    Log.e(TAG, "Failed to toggle like: $error")
                    // TODO: Queue for sync
                    onComplete(false, false, 0)
                }
            )
        } else {
            // TODO: Save locally and queue for sync
            onComplete(false, false, 0)
        }
    }

    // ========== USER OPERATIONS ==========

    /**
     * Get user (cache-first)
     */
    fun getUser(
        userId: Int?,
        email: String?,
        username: String?,
        onComplete: (user: Map<String, Any>?) -> Unit
    ) {
        // Check cache first
        userId?.let {
            val cachedUser = dbHelper.getCachedUser(it)
            if (cachedUser != null) {
                onComplete(cachedUser)
                // Still fetch from server to update cache
            }
        }

        if (!apiClient.isNetworkAvailable()) {
            onComplete(null)
            return
        }

        val params = mutableMapOf<String, String>()
        userId?.let { params["userId"] = it.toString() }
        email?.let { params["email"] = it }
        username?.let { params["username"] = it }

        val url = apiClient.buildUrlWithParams(ApiConfig.GET_USER, params)

        apiClient.get(
            url,
            onSuccess = { response ->
                val success = response.optBoolean("success", false)
                if (success) {
                    val userObj = response.optJSONObject("user")
                    userObj?.let {
                        val user = mutableMapOf<String, Any>()
                        user["id"] = it.optInt("id")
                        user["email"] = it.optString("email")
                        user["username"] = it.optString("username")
                        user["firstName"] = it.optString("firstName")
                        user["lastName"] = it.optString("lastName")
                        user["profilePicBase64"] = it.optString("profilePicBase64")
                        user["followersCount"] = it.optInt("followersCount")
                        user["followingCount"] = it.optInt("followingCount")
                        user["postsCount"] = it.optInt("postsCount")
                        
                        // Update cache
                        dbHelper.cacheUser(
                            it.optInt("id"),
                            it.optString("email"),
                            it.optString("username"),
                            it.optString("firstName"),
                            it.optString("lastName"),
                            it.optString("profilePicBase64")
                        )
                        
                        onComplete(user)
                    }
                } else {
                    onComplete(null)
                }
            },
            onError = { error ->
                Log.e(TAG, "Failed to fetch user: $error")
                onComplete(null)
            }
        )
    }

    // ========== DM OPERATIONS ==========

    fun getMutualFollows(userId: Int, onComplete: (users: List<Map<String, Any>>) -> Unit) {
        if (apiClient.isNetworkAvailable()) {
            val url = "${ApiConfig.GET_MUTUAL_FOLLOWS}?userId=$userId"
            apiClient.get(
                url,
                onSuccess = { response ->
                    val success = response.optBoolean("success", false)
                    if (success) {
                        val usersArray = response.optJSONArray("users")
                        val users = mutableListOf<Map<String, Any>>()
                        usersArray?.let {
                            for (i in 0 until it.length()) {
                                val userObj = it.getJSONObject(i)
                                val user = mutableMapOf<String, Any>()
                                user["id"] = userObj.optInt("id")
                                user["username"] = userObj.optString("username")
                                user["profilePicBase64"] = userObj.optString("profilePicBase64")
                                users.add(user)
                            }
                        }
                        onComplete(users)
                    } else {
                        onComplete(emptyList())
                    }
                },
                onError = {
                    onComplete(emptyList())
                }
            )
        } else {
            // TODO: Implement local cache for mutual follows if needed
            onComplete(emptyList())
        }
    }

    fun sendMessage(
        senderId: Int,
        receiverId: Int,
        text: String,
        mediaBase64: String = "",
        mediaType: String = "",
        onComplete: (success: Boolean, messageId: Long) -> Unit
    ) {
        // Save locally first
        val localId = dbHelper.insertMessage(senderId, receiverId, text, mediaBase64, mediaType)
        
        if (apiClient.isNetworkAvailable()) {
            val params = mapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "text" to text,
                "mediaBase64" to mediaBase64,
                "mediaType" to mediaType
            )

            apiClient.post(
                ApiConfig.SEND_MESSAGE,
                params,
                onSuccess = { response ->
                    val success = response.optBoolean("success", false)
                    if (success) {
                        val serverId = response.optString("messageId") // PHP returns ID
                        dbHelper.updateMessageServerId(localId, serverId)
                        onComplete(true, localId)
                    } else {
                        syncManager.queueForSync("MESSAGE", localId, params, ApiConfig.SEND_MESSAGE)
                        onComplete(true, localId)
                    }
                },
                onError = {
                    syncManager.queueForSync("MESSAGE", localId, params, ApiConfig.SEND_MESSAGE)
                    onComplete(true, localId)
                }
            )
        } else {
            val params = mapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "text" to text,
                "mediaBase64" to mediaBase64,
                "mediaType" to mediaType
            )
            syncManager.queueForSync("MESSAGE", localId, params, ApiConfig.SEND_MESSAGE)
            onComplete(true, localId)
        }
    }

    fun editMessage(localId: Long, messageId: String, newText: String) {
        // Update locally
        dbHelper.updateMessageText(localId, newText)

        if (apiClient.isNetworkAvailable()) {
            val params = mapOf(
                "messageId" to messageId,
                "newText" to newText
            )
            apiClient.post(ApiConfig.EDIT_MESSAGE, params, onSuccess = {}, onError = {})
        }
        // If offline, sync manager should handle it if we add support for EDIT_MESSAGE sync
        // For now, simple offline support (local update only until online)
    }

    fun deleteMessage(localId: Long, messageId: String) {
        // Update locally
        dbHelper.deleteMessage(localId)

        if (apiClient.isNetworkAvailable()) {
            val params = mapOf("messageId" to messageId)
            apiClient.post(ApiConfig.DELETE_MESSAGE, params, onSuccess = {}, onError = {})
        }
    }

    fun getMessages(
        userId: Int,
        otherUserId: Int,
        lastId: Long = 0,
        onComplete: (messages: List<Map<String, Any>>) -> Unit
    ) {
        // If initial load, return local messages first
        if (lastId == 0L) {
            val localMessages = dbHelper.getMessages(userId, otherUserId)
            onComplete(localMessages)
        }

        if (apiClient.isNetworkAvailable()) {
            val url = "${ApiConfig.GET_MESSAGES}?userId=$userId&otherUserId=$otherUserId&lastId=$lastId"
            apiClient.get(
                url,
                onSuccess = { response ->
                    val success = response.optBoolean("success", false)
                    if (success) {
                        val messagesArray = response.optJSONArray("messages")
                        
                        messagesArray?.let {
                            for (i in 0 until it.length()) {
                                val msg = it.getJSONObject(i)
                                val serverId = msg.optString("id")
                                val senderId = msg.optInt("senderId")
                                val receiverId = msg.optInt("receiverId")
                                val text = msg.optString("text")
                                val mediaBase64 = msg.optString("mediaBase64")
                                val mediaType = msg.optString("mediaType")
                                val timestamp = msg.optLong("timestamp")
                                val isEdited = msg.optInt("isEdited")
                                val isDeleted = msg.optInt("isDeleted")

                                // Save to local DB
                                dbHelper.saveSyncedMessage(
                                    serverId,
                                    senderId,
                                    receiverId,
                                    text,
                                    mediaBase64,
                                    mediaType,
                                    timestamp,
                                    isEdited,
                                    isDeleted
                                )
                            }
                            
                            // Always fetch complete message list after syncing
                            if (lastId == 0L) {
                                // Initial load - return all messages
                                val allMessages = dbHelper.getMessages(userId, otherUserId)
                                onComplete(allMessages)
                            } else {
                                // Incremental load - return only new messages
                                val newMessages = dbHelper.getMessagesAfter(userId, otherUserId, lastId)
                                onComplete(newMessages)
                            }
                        }
                    }
                },
                onError = {}
            )
        }
    }

    // ========== SYNC OPERATIONS ==========

    /**
     * Trigger manual sync
     */
    fun triggerSync(onComplete: ((success: Boolean, synced: Int, failed: Int) -> Unit)? = null) {
        syncManager.startSync(onComplete)
    }

    /**
     * Get sync status
     */
    fun getSyncStatus(): Map<String, Int> {
        return syncManager.getSyncStatus()
    }
}
