package com.faujipanda.i230665_i230026

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject
import kotlin.math.min
import kotlin.math.pow

/**
 * Sync Manager
 * Handles offline-to-online data synchronization with retry logic
 */
class SyncManager(private val context: Context) {

    private val apiClient = ApiClient(context)
    private val dbHelper = LocalDatabaseHelper(context)
    private val gson = Gson()
    private var isSyncing = false

    companion object {
        private const val TAG = "SyncManager"
        private const val MAX_RETRIES = 5
        private const val BASE_DELAY = 1000L // 1 second
    }

    /**
     * Start sync process for all pending items
     */
    fun startSync(onComplete: ((success: Boolean, synced: Int, failed: Int) -> Unit)? = null) {
        if (isSyncing) {
            Log.d(TAG, "Sync already in progress")
            return
        }

        if (!apiClient.isNetworkAvailable()) {
            Log.d(TAG, "No network available, skipping sync")
            onComplete?.invoke(false, 0, 0)
            return
        }

        isSyncing = true
        Log.d(TAG, "Starting sync process")

        val pendingItems = dbHelper.getPendingSyncItems()
        if (pendingItems.isEmpty()) {
            Log.d(TAG, "No pending items to sync")
            isSyncing = false
            onComplete?.invoke(true, 0, 0)
            return
        }

        var syncedCount = 0
        var failedCount = 0
        var processedCount = 0

        pendingItems.forEach { item ->
            val id = item["id"] as Long
            val operationType = item["operationType"] as String
            val localId = item["localId"] as Long
            val payload = item["payload"] as String
            val endpoint = item["endpoint"] as String
            val retryCount = item["retryCount"] as Int

            if (retryCount >= MAX_RETRIES) {
                Log.w(TAG, "Max retries reached for item $id, marking as failed")
                dbHelper.updateSyncItemStatus(id, "failed", retryCount)
                failedCount++
                processedCount++
                
                if (processedCount == pendingItems.size) {
                    finishSync(onComplete, syncedCount, failedCount)
                }
                return@forEach
            }

            // Mark as processing
            dbHelper.updateSyncItemStatus(id, "processing", retryCount)

            // Parse payload
            val params = try {
                gson.fromJson(payload, Map::class.java) as Map<String, Any>
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse payload for item $id", e)
                dbHelper.updateSyncItemStatus(id, "failed", retryCount + 1)
                failedCount++
                processedCount++
                
                if (processedCount == pendingItems.size) {
                    finishSync(onComplete, syncedCount, failedCount)
                }
                return@forEach
            }

            // Execute API call
            apiClient.post(
                endpoint,
                params,
                onSuccess = { response ->
                    val success = response.optBoolean("success", false)
                    if (success) {
                        Log.d(TAG, "Successfully synced item $id ($operationType)")
                        
                        // Update local database with server response
                        when (operationType) {
                            "POST" -> {
                                val postId = response.optString("postId")
                                if (postId.isNotEmpty()) {
                                    dbHelper.updatePostServerId(localId, postId)
                                }
                            }
                            // Add other operation types as needed
                        }
                        
                        // Remove from sync queue
                        dbHelper.deleteSyncItem(id)
                        syncedCount++
                    } else {
                        Log.w(TAG, "Server returned success=false for item $id")
                        dbHelper.updateSyncItemStatus(id, "pending", retryCount + 1)
                        failedCount++
                    }
                    
                    processedCount++
                    if (processedCount == pendingItems.size) {
                        finishSync(onComplete, syncedCount, failedCount)
                    }
                },
                onError = { error ->
                    Log.e(TAG, "Failed to sync item $id: $error")
                    
                    // Calculate exponential backoff delay
                    val delay = calculateBackoffDelay(retryCount)
                    Log.d(TAG, "Will retry item $id after ${delay}ms")
                    
                    dbHelper.updateSyncItemStatus(id, "pending", retryCount + 1)
                    failedCount++
                    
                    processedCount++
                    if (processedCount == pendingItems.size) {
                        finishSync(onComplete, syncedCount, failedCount)
                    }
                }
            )
        }
    }

    private fun finishSync(onComplete: ((Boolean, Int, Int) -> Unit)?, syncedCount: Int, failedCount: Int) {
        isSyncing = false
        val success = failedCount == 0
        Log.d(TAG, "Sync complete: synced=$syncedCount, failed=$failedCount")
        onComplete?.invoke(success, syncedCount, failedCount)
    }

    /**
     * Calculate exponential backoff delay
     */
    private fun calculateBackoffDelay(retryCount: Int): Long {
        val exponentialDelay = BASE_DELAY * (2.0.pow(retryCount.toDouble())).toLong()
        return min(exponentialDelay, 60000L) // Max 60 seconds
    }

    /**
     * Add item to sync queue
     */
    fun queueForSync(operationType: String, localId: Long, payload: Map<String, Any>, endpoint: String) {
        val payloadJson = gson.toJson(payload)
        dbHelper.addToSyncQueue(operationType, localId, payloadJson, endpoint)
        Log.d(TAG, "Added $operationType to sync queue (localId=$localId)")
    }

    /**
     * Get sync queue status
     */
    fun getSyncStatus(): Map<String, Int> {
        val pending = dbHelper.getPendingSyncItems().size
        return mapOf(
            "pending" to pending,
            "syncing" to if (isSyncing) 1 else 0
        )
    }

    /**
     * Clear completed sync items
     */
    fun clearCompletedItems() {
        // This is handled automatically when items are synced successfully
        Log.d(TAG, "Completed items are automatically cleared")
    }
}
