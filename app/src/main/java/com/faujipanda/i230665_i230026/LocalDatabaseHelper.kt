package com.faujipanda.i230665_i230026

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Local SQLite Database Helper for Offline Storage
 * Manages local caching and sync queue
 */
class LocalDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sociallypersonal_local.db"
        private const val DATABASE_VERSION = 3

        // Table names
        const val TABLE_LOCAL_USERS = "local_users"
        const val TABLE_LOCAL_POSTS = "local_posts"
        const val TABLE_LOCAL_STORIES = "local_stories"
        const val TABLE_LOCAL_STORY_CHUNKS = "local_story_chunks"
        const val TABLE_LOCAL_COMMENTS = "local_comments"
        const val TABLE_LOCAL_LIKES = "local_likes"
        const val TABLE_LOCAL_FOLLOWS = "local_follows"
        const val TABLE_LOCAL_MESSAGES = "local_messages"
        const val TABLE_SYNC_QUEUE = "sync_queue"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create local_users table
        db.execSQL(
            """
            CREATE TABLE $TABLE_LOCAL_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                email TEXT,
                username TEXT,
                first_name TEXT,
                last_name TEXT,
                profile_pic_base64 TEXT,
                last_synced INTEGER
            )
        """
        )

        // Create local_posts table
        db.execSQL(
            """
            CREATE TABLE $TABLE_LOCAL_POSTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                server_post_id TEXT,
                user_id INTEGER,
                media_base64 TEXT,
                media_type TEXT,
                caption TEXT,
                timestamp INTEGER,
                likes INTEGER DEFAULT 0,
                comments_count INTEGER DEFAULT 0,
                is_synced INTEGER DEFAULT 0,
                sync_error TEXT
            )
        """
        )

        // Create local_stories table
        db.execSQL(
            """
            CREATE TABLE $TABLE_LOCAL_STORIES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                server_story_id TEXT,
                user_id INTEGER,
                media_base64 TEXT,
                media_type TEXT,
                timestamp INTEGER,
                chunk_count INTEGER,
                is_synced INTEGER DEFAULT 0
            )
        """
        )

        // Create local_story_chunks table
        db.execSQL(
            """
            CREATE TABLE $TABLE_LOCAL_STORY_CHUNKS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                local_story_id INTEGER,
                chunk_index INTEGER,
                chunk_data TEXT,
                FOREIGN KEY(local_story_id) REFERENCES $TABLE_LOCAL_STORIES(id) ON DELETE CASCADE
            )
        """
        )

        // Create local_comments table
        db.execSQL(
            """
            CREATE TABLE $TABLE_LOCAL_COMMENTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                server_comment_id TEXT,
                post_id TEXT,
                user_id INTEGER,
                text TEXT,
                timestamp INTEGER,
                is_synced INTEGER DEFAULT 0
            )
        """
        )

        // Create local_likes table
        db.execSQL(
            """
            CREATE TABLE $TABLE_LOCAL_LIKES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                post_id TEXT,
                user_id INTEGER,
                is_liked INTEGER,
                timestamp INTEGER,
                is_synced INTEGER DEFAULT 0
            )
        """
        )

        // Create local_follows table
        db.execSQL(
            """
            CREATE TABLE $TABLE_LOCAL_FOLLOWS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                target_user_id INTEGER,
                is_following INTEGER,
                timestamp INTEGER,
                is_synced INTEGER DEFAULT 0
            )
        """
        )

        // Create local_messages table
        db.execSQL(
            """
            CREATE TABLE $TABLE_LOCAL_MESSAGES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                server_message_id TEXT,
                sender_id INTEGER,
                receiver_id INTEGER,
                text TEXT,
                media_base64 TEXT,
                media_type TEXT,
                timestamp INTEGER,
                is_read INTEGER DEFAULT 0,
                is_edited INTEGER DEFAULT 0,
                is_deleted INTEGER DEFAULT 0,
                is_synced INTEGER DEFAULT 0
            )
        """
        )

        // Create sync_queue table
        db.execSQL(
            """
            CREATE TABLE $TABLE_SYNC_QUEUE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                operation_type TEXT,
                local_id INTEGER,
                payload TEXT,
                endpoint TEXT,
                retry_count INTEGER DEFAULT 0,
                last_attempt INTEGER,
                created_at INTEGER,
                status TEXT DEFAULT 'pending'
            )
        """
        )

        // Create indexes for better performance
        db.execSQL("CREATE INDEX idx_local_posts_user ON $TABLE_LOCAL_POSTS(user_id)")
        db.execSQL("CREATE INDEX idx_local_posts_synced ON $TABLE_LOCAL_POSTS(is_synced)")
        db.execSQL("CREATE INDEX idx_sync_queue_status ON $TABLE_SYNC_QUEUE(status)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop all tables and recreate
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCAL_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCAL_POSTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCAL_STORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCAL_STORY_CHUNKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCAL_COMMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCAL_LIKES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCAL_FOLLOWS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCAL_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SYNC_QUEUE")
        onCreate(db)
    }

    // ========== POST OPERATIONS ==========

    fun insertPost(userId: Int, mediaBase64: String, mediaType: String, caption: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("media_base64", mediaBase64)
            put("media_type", mediaType)
            put("caption", caption)
            put("timestamp", System.currentTimeMillis())
            put("is_synced", 0)
        }
        return db.insert(TABLE_LOCAL_POSTS, null, values)
    }

    fun updatePostServerId(localId: Long, serverId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("server_post_id", serverId)
            put("is_synced", 1)
        }
        db.update(TABLE_LOCAL_POSTS, values, "id = ?", arrayOf(localId.toString()))
    }

    fun getUnsyncedPosts(): List<Map<String, Any>> {
        val posts = mutableListOf<Map<String, Any>>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LOCAL_POSTS,
            null,
            "is_synced = ?",
            arrayOf("0"),
            null, null, "timestamp ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val post = mutableMapOf<String, Any>()
                post["id"] = it.getLong(it.getColumnIndexOrThrow("id"))
                post["userId"] = it.getInt(it.getColumnIndexOrThrow("user_id"))
                post["mediaBase64"] = it.getString(it.getColumnIndexOrThrow("media_base64"))
                post["mediaType"] = it.getString(it.getColumnIndexOrThrow("media_type"))
                post["caption"] = it.getString(it.getColumnIndexOrThrow("caption"))
                post["timestamp"] = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                posts.add(post)
            }
        }
        return posts
    }

    // ========== STORY OPERATIONS ==========

    fun insertStory(userId: Int, mediaBase64: String, mediaType: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("media_base64", mediaBase64)
            put("media_type", mediaType)
            put("timestamp", System.currentTimeMillis())
            put("chunk_count", 0) // Not used anymore but keeping for schema compatibility
            put("is_synced", 0)
        }
        return db.insert(TABLE_LOCAL_STORIES, null, values)
    }

    fun updateStoryServerId(localId: Long, serverId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("server_story_id", serverId)
            put("is_synced", 1)
        }
        db.update(TABLE_LOCAL_STORIES, values, "id = ?", arrayOf(localId.toString()))
    }

    fun getUnsyncedStories(): List<Map<String, Any>> {
        val stories = mutableListOf<Map<String, Any>>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LOCAL_STORIES,
            null,
            "is_synced = ?",
            arrayOf("0"),
            null, null, "timestamp ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val story = mutableMapOf<String, Any>()
                story["id"] = it.getLong(it.getColumnIndexOrThrow("id"))
                story["userId"] = it.getInt(it.getColumnIndexOrThrow("user_id"))
                story["mediaBase64"] = it.getString(it.getColumnIndexOrThrow("media_base64"))
                story["mediaType"] = it.getString(it.getColumnIndexOrThrow("media_type"))
                story["timestamp"] = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                stories.add(story)
            }
        }
        return stories
    }

    // ========== MESSAGE OPERATIONS ==========

    fun insertMessage(
        senderId: Int,
        receiverId: Int,
        text: String,
        mediaBase64: String = "",
        mediaType: String = "",
        isSynced: Int = 0
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("sender_id", senderId)
            put("receiver_id", receiverId)
            put("text", text)
            put("media_base64", mediaBase64)
            put("media_type", mediaType)
            put("timestamp", System.currentTimeMillis())
            put("is_synced", isSynced)
        }
        return db.insert(TABLE_LOCAL_MESSAGES, null, values)
    }

    fun updateMessageServerId(localId: Long, serverId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("server_message_id", serverId)
            put("is_synced", 1)
        }
        db.update(TABLE_LOCAL_MESSAGES, values, "id = ?", arrayOf(localId.toString()))
    }

    fun updateMessageText(localId: Long, newText: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("text", newText)
            put("is_edited", 1)
            put("is_synced", 0) // Mark as unsynced to trigger update
        }
        db.update(TABLE_LOCAL_MESSAGES, values, "id = ?", arrayOf(localId.toString()))
    }

    fun deleteMessage(localId: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_deleted", 1)
            put("is_synced", 0) // Mark as unsynced to trigger delete
        }
        db.update(TABLE_LOCAL_MESSAGES, values, "id = ?", arrayOf(localId.toString()))
    }

    fun saveSyncedMessage(
        serverId: String,
        senderId: Int,
        receiverId: Int,
        text: String,
        mediaBase64: String,
        mediaType: String,
        timestamp: Long,
        isEdited: Int,
        isDeleted: Int
    ) {
        val db = writableDatabase
        // Check if exists
        val cursor = db.query(
            TABLE_LOCAL_MESSAGES,
            arrayOf("id"),
            "server_message_id = ?",
            arrayOf(serverId),
            null, null, null
        )
        
        val exists = cursor.count > 0
        cursor.close()
        
        val values = ContentValues().apply {
            put("server_message_id", serverId)
            put("sender_id", senderId)
            put("receiver_id", receiverId)
            put("text", text)
            put("media_base64", mediaBase64)
            put("media_type", mediaType)
            put("timestamp", timestamp)
            put("is_edited", isEdited)
            put("is_deleted", isDeleted)
            put("is_synced", 1)
        }

        if (exists) {
            db.update(TABLE_LOCAL_MESSAGES, values, "server_message_id = ?", arrayOf(serverId))
        } else {
            db.insert(TABLE_LOCAL_MESSAGES, null, values)
        }
    }

    fun getMessages(userId: Int, otherUserId: Int): List<Map<String, Any>> {
        val messages = mutableListOf<Map<String, Any>>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LOCAL_MESSAGES,
            null,
            "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) AND is_deleted = 0",
            arrayOf(userId.toString(), otherUserId.toString(), otherUserId.toString(), userId.toString()),
            null, null, "timestamp ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val message = mutableMapOf<String, Any>()
                message["id"] = it.getLong(it.getColumnIndexOrThrow("id"))
                message["serverMessageId"] = it.getString(it.getColumnIndexOrThrow("server_message_id")) ?: ""
                message["senderId"] = it.getInt(it.getColumnIndexOrThrow("sender_id"))
                message["receiverId"] = it.getInt(it.getColumnIndexOrThrow("receiver_id"))
                message["text"] = it.getString(it.getColumnIndexOrThrow("text"))
                message["mediaBase64"] = it.getString(it.getColumnIndexOrThrow("media_base64"))
                message["mediaType"] = it.getString(it.getColumnIndexOrThrow("media_type"))
                message["timestamp"] = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                message["isEdited"] = it.getInt(it.getColumnIndexOrThrow("is_edited"))
                messages.add(message)
            }
        }
        return messages
    }

    fun getMessagesAfter(userId: Int, otherUserId: Int, lastServerId: Long): List<Map<String, Any>> {
        // Reuse getMessages to fetch all, then filter in memory to avoid SQLite CAST issues
        val allMessages = getMessages(userId, otherUserId)
        return allMessages.filter {
            val serverIdStr = it["serverMessageId"] as? String
            val serverId = serverIdStr?.toLongOrNull() ?: 0L
            serverId > lastServerId
        }
    }

    // ========== SYNC QUEUE OPERATIONS ==========

    fun addToSyncQueue(
        operationType: String,
        localId: Long,
        payload: String,
        endpoint: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("operation_type", operationType)
            put("local_id", localId)
            put("payload", payload)
            put("endpoint", endpoint)
            put("created_at", System.currentTimeMillis())
            put("status", "pending")
        }
        return db.insert(TABLE_SYNC_QUEUE, null, values)
    }

    fun getPendingSyncItems(): List<Map<String, Any>> {
        val items = mutableListOf<Map<String, Any>>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SYNC_QUEUE,
            null,
            "status = ?",
            arrayOf("pending"),
            null, null, "created_at ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val item = mutableMapOf<String, Any>()
                item["id"] = it.getLong(it.getColumnIndexOrThrow("id"))
                item["operationType"] = it.getString(it.getColumnIndexOrThrow("operation_type"))
                item["localId"] = it.getLong(it.getColumnIndexOrThrow("local_id"))
                item["payload"] = it.getString(it.getColumnIndexOrThrow("payload"))
                item["endpoint"] = it.getString(it.getColumnIndexOrThrow("endpoint"))
                item["retryCount"] = it.getInt(it.getColumnIndexOrThrow("retry_count"))
                items.add(item)
            }
        }
        return items
    }

    fun updateSyncItemStatus(id: Long, status: String, retryCount: Int = 0) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("status", status)
            put("retry_count", retryCount)
            put("last_attempt", System.currentTimeMillis())
        }
        db.update(TABLE_SYNC_QUEUE, values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteSyncItem(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_SYNC_QUEUE, "id = ?", arrayOf(id.toString()))
    }

    // ========== CACHE OPERATIONS ==========

    fun cacheUser(
        userId: Int,
        email: String,
        username: String,
        firstName: String,
        lastName: String,
        profilePic: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("email", email)
            put("username", username)
            put("first_name", firstName)
            put("last_name", lastName)
            put("profile_pic_base64", profilePic)
            put("last_synced", System.currentTimeMillis())
        }

        // Insert or replace
        db.insertWithOnConflict(
            TABLE_LOCAL_USERS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getCachedUser(userId: Int): Map<String, Any>? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LOCAL_USERS,
            null,
            "user_id = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return mapOf(
                    "userId" to it.getInt(it.getColumnIndexOrThrow("user_id")),
                    "email" to it.getString(it.getColumnIndexOrThrow("email")),
                    "username" to it.getString(it.getColumnIndexOrThrow("username")),
                    "firstName" to it.getString(it.getColumnIndexOrThrow("first_name")),
                    "lastName" to it.getString(it.getColumnIndexOrThrow("last_name")),
                    "profilePicBase64" to it.getString(it.getColumnIndexOrThrow("profile_pic_base64"))
                )
            }
        }
        return null
    }

    fun clearOldCache(daysOld: Int = 7) {
        val db = writableDatabase
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)

        db.delete(TABLE_LOCAL_USERS, "last_synced < ?", arrayOf(cutoffTime.toString()))
        db.delete(
            TABLE_LOCAL_POSTS,
            "timestamp < ? AND is_synced = ?",
            arrayOf(cutoffTime.toString(), "1")
        )
    }
}
