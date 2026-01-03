package com.faujipanda.i230665_i230026

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class page11 : AppCompatActivity() {
    
    private var userId: Int = 0
    private lateinit var apiClient: ApiClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationsAdapter
    
    private lateinit var navHome: ImageView
    private lateinit var navSearch: ImageView
    private lateinit var navCreate: ImageView
    private lateinit var navLike: ImageView
    private lateinit var navProfile: CircleImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page11)
        
        apiClient = ApiClient(this)
        
        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = intent.getIntExtra("userId", shared.getInt("userId", 0))
        
        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        navHome = findViewById(R.id.navHome)
        navSearch = findViewById(R.id.navSearch)
        navCreate = findViewById(R.id.navCreate)
        navLike = findViewById(R.id.navLike)
        navProfile = findViewById(R.id.navProfile)
        
        // Setup RecyclerView
        adapter = NotificationsAdapter(mutableListOf(), 
            onAccept = { notification -> handleAccept(notification) },
            onReject = { notification -> handleReject(notification) },
            onFollowBack = { notification -> handleFollowBack(notification) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        // Setup bottom navigation
        BottomNavHelper.setupBottomNav(
            this,
            "like",
            navHome,
            navSearch,
            navCreate,
            navLike,
            navProfile
        )
        
        loadNotifications()
        loadUserProfilePicture()
    }
    
    override fun onResume() {
        super.onResume()
        loadNotifications()
        startPolling()
    }
    
    override fun onPause() {
        super.onPause()
        stopPolling()
    }
    
    private val pollingHandler = Handler(Looper.getMainLooper())
    private val pollingRunnable = object : Runnable {
        override fun run() {
            loadNotifications()
            pollingHandler.postDelayed(this, 5000) // Poll every 5 seconds
        }
    }
    
    private fun startPolling() {
        pollingHandler.postDelayed(pollingRunnable, 5000)
    }
    
    private fun stopPolling() {
        pollingHandler.removeCallbacks(pollingRunnable)
    }
    
    private fun loadNotifications() {
        val url = apiClient.buildUrlWithParams(ApiConfig.GET_NOTIFICATIONS, mapOf(
            "userId" to userId.toString()
        ))
        
        apiClient.get(url, onSuccess = { response ->
            val data = response.getJSONObject("data")
            val notificationsArray = data.getJSONArray("notifications")
            val notifications = mutableListOf<Notification>()
            
            for (i in 0 until notificationsArray.length()) {
                val notif = notificationsArray.getJSONObject(i)
                val sender = notif.getJSONObject("sender")
                
                // Parse post data if available
                val postData = if (notif.has("post") && !notif.isNull("post")) {
                    val post = notif.getJSONObject("post")
                    NotificationPost(
                        mediaBase64 = post.optString("mediaBase64", ""),
                        likes = post.optInt("likes", 0),
                        comments = post.optInt("comments", 0)
                    )
                } else null
                
                notifications.add(
                    Notification(
                        id = notif.getInt("id"),
                        type = notif.getString("type"),
                        postId = if (notif.isNull("postId")) null else notif.getInt("postId"),
                        isRead = notif.getBoolean("isRead"),
                        timestamp = notif.getLong("timestamp"),
                        sender = NotificationSender(
                            id = sender.getInt("id"),
                            username = sender.getString("username"),
                            firstName = sender.getString("firstName"),
                            lastName = sender.getString("lastName"),
                            profilePicBase64 = sender.optString("profilePicBase64", "")
                        ),
                        post = postData
                    )
                )
            }
            
            adapter.updateNotifications(notifications)
        }, onError = { error ->
            // Silently fail during polling to avoid toast spam
        })
    }
    
    private fun loadUserProfilePicture() {
        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        val profilePicBase64 = shared.getString("profilePicBase64", null)
        if (!profilePicBase64.isNullOrEmpty()) {
            try {
                val cleanBase64 = profilePicBase64.replace("data:image/jpeg;base64,", "")
                val decodedString = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                val decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                navProfile.setImageBitmap(decodedByte)
            } catch (e: Exception) {
                navProfile.setImageResource(R.drawable.pfp_image)
            }
        } else {
            navProfile.setImageResource(R.drawable.pfp_image)
        }
    }
    
    private fun handleAccept(notification: Notification) {
        val params = mapOf(
            "userId" to userId,
            "senderId" to notification.sender.id,
            "action" to "accept"
        )
        
        apiClient.post(ApiConfig.RESPOND_FOLLOW_REQUEST, params, onSuccess = { response ->
            Toast.makeText(this, "Follow request accepted", Toast.LENGTH_SHORT).show()
            loadNotifications() // Refresh
        }, onError = { error ->
            Toast.makeText(this, "Failed to accept: $error", Toast.LENGTH_SHORT).show()
        })
    }
    
    private fun handleReject(notification: Notification) {
        val params = mapOf(
            "userId" to userId,
            "senderId" to notification.sender.id,
            "action" to "reject"
        )
        
        apiClient.post(ApiConfig.RESPOND_FOLLOW_REQUEST, params, onSuccess = { response ->
            Toast.makeText(this, "Follow request rejected", Toast.LENGTH_SHORT).show()
            loadNotifications() // Refresh
        }, onError = { error ->
            Toast.makeText(this, "Failed to reject: $error", Toast.LENGTH_SHORT).show()
        })
    }
    
    private fun handleFollowBack(notification: Notification) {
        // First check if already following
        val checkUrl = apiClient.buildUrlWithParams(ApiConfig.CHECK_FOLLOWING, mapOf(
            "userId" to userId.toString(),
            "targetUserId" to notification.sender.id.toString()
        ))
        
        apiClient.get(checkUrl, onSuccess = { response ->
            val data = response.getJSONObject("data")
            val status = data.optString("status", "none")
            
            if (status == "accepted") {
                // Already following - unfollow
                val params = mapOf(
                    "userId" to userId,
                    "targetUserId" to notification.sender.id
                )
                apiClient.post(ApiConfig.UNFOLLOW_USER, params, onSuccess = { response ->
                    Toast.makeText(this, "Unfollowed", Toast.LENGTH_SHORT).show()
                    loadNotifications() // Refresh to update button
                }, onError = { error ->
                    Toast.makeText(this, "Failed to unfollow: $error", Toast.LENGTH_SHORT).show()
                })
            } else {
                // Not following - send follow request
                val params = mapOf(
                    "userId" to userId,
                    "targetUserId" to notification.sender.id
                )
                apiClient.post(ApiConfig.FOLLOW_USER, params, onSuccess = { response ->
                    Toast.makeText(this, "Follow request sent", Toast.LENGTH_SHORT).show()
                    loadNotifications() // Refresh
                }, onError = { error ->
                    Toast.makeText(this, "Failed to follow: $error", Toast.LENGTH_SHORT).show()
                })
            }
        }, onError = { error ->
            Toast.makeText(this, "Failed to check status: $error", Toast.LENGTH_SHORT).show()
        })
    }
}