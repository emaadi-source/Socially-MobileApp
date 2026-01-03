package com.faujipanda.i230665_i230026

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray

class page5 : AppCompatActivity() {

    private lateinit var apiClient: ApiClient
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var repository: OfflineRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedWithStoriesAdapter
    private val feedItems = mutableListOf<FeedPost>()

    private lateinit var navHome: ImageView
    private lateinit var navSearch: ImageView
    private lateinit var navCreate: ImageView
    private lateinit var navLike: ImageView
    private lateinit var navProfile: CircleImageView
    private lateinit var btnCamera: ImageView
    private lateinit var btnMessages: ImageView
    private lateinit var appTitle: TextView

    private var pressStartTime: Long = 0
    private var userId: Int = 0
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_page5)

            apiClient = ApiClient(this)
            repository = OfflineRepository(this)

            // Initialize NetworkMonitor
            networkMonitor = NetworkMonitor(this) { isConnected ->
                if (isConnected) {
                    runOnUiThread {
                        Toast.makeText(this, "Back online! Syncing pending items...", Toast.LENGTH_SHORT).show()
                        repository.triggerSync { success, synced, failed ->
                            if (success && synced > 0) {
                                runOnUiThread {
                                    Toast.makeText(this, "Synced $synced items", Toast.LENGTH_SHORT).show()
                                    loadPosts()
                                    adapter.refreshStories(userId)
                                }
                            }
                        }
                    }
                }
            }
            networkMonitor.startMonitoring()

            val shared = getSharedPreferences("user_session", MODE_PRIVATE)
            userId = intent.getIntExtra("userId", shared.getInt("userId", 0))
            username = intent.getStringExtra("username") ?: shared.getString("lastUsername", "")

            if (userId == 0) {
                Toast.makeText(this, "Session lost. Please log in again.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, page4::class.java))
                finish()
                return
            }

            recyclerView = findViewById(R.id.recyclerFeed)
            recyclerView.layoutManager = LinearLayoutManager(this)
            adapter = FeedWithStoriesAdapter(feedItems, userId, apiClient)
            recyclerView.adapter = adapter

            navHome = findViewById(R.id.navHome)
            navSearch = findViewById(R.id.navSearch)
            navCreate = findViewById(R.id.navCreate)
            navLike = findViewById(R.id.navLike)
            navProfile = findViewById(R.id.navProfile)
            btnCamera = findViewById(R.id.btnCamera)
            btnMessages = findViewById(R.id.btnDM)
            appTitle = findViewById(R.id.title)

            // Setup bottom navigation using helper
            BottomNavHelper.setupBottomNav(
                this,
                "home",
                navHome,
                navSearch,
                navCreate,
                navLike,
                navProfile
            )

            loadPosts()
            loadUserProfilePicture()

            // Start active status heartbeat
            startActiveStatusHeartbeat()

            appTitle.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> pressStartTime = System.currentTimeMillis()
                    MotionEvent.ACTION_UP -> {
                        val duration = System.currentTimeMillis() - pressStartTime
                        if (duration >= 3000) logOutUser()
                    }
                }
                true
            }
            btnCamera.setOnClickListener {
                val i = Intent(this, AddStoryActivity::class.java)
                i.putExtra("userId", userId)
                startActivity(i)
            }

            btnMessages.setOnClickListener {
                val i = Intent(this, page8::class.java)
                i.putExtra("userId", userId)
                startActivity(i)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing feed: ${e.message}", Toast.LENGTH_LONG).show()
            // Fallback to page4
            startActivity(Intent(this, page4::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::networkMonitor.isInitialized) {
            networkMonitor.stopMonitoring()
        }
    }

    private val heartbeatHandler = Handler(Looper.getMainLooper())
    private val heartbeatRunnable = object : Runnable {
        override fun run() {
            updateActiveStatus(true)
            heartbeatHandler.postDelayed(this, 30000) // Every 30 seconds
        }
    }

    private fun startActiveStatusHeartbeat() {
        updateActiveStatus(true)
        heartbeatHandler.postDelayed(heartbeatRunnable, 30000)
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
        loadUserProfilePicture()
        adapter.refreshStories(userId)
        
        // Start heartbeat
        updateActiveStatus(true)
        heartbeatHandler.postDelayed(heartbeatRunnable, 30000)
        
        // Start feed polling for real-time likes/comments
        feedPollingHandler.postDelayed(feedPollingRunnable, 5000)
    }

    override fun onPause() {
        super.onPause()
        // Stop heartbeat
        heartbeatHandler.removeCallbacks(heartbeatRunnable)
        updateActiveStatus(false)
        
        // Stop feed polling
        feedPollingHandler.removeCallbacks(feedPollingRunnable)
    }
    
    private val feedPollingHandler = Handler(Looper.getMainLooper())
    private val feedPollingRunnable = object : Runnable {
        override fun run() {
            loadPosts()
            feedPollingHandler.postDelayed(this, 5000) // Poll every 5 seconds
        }
    }

    private fun updateActiveStatus(isActive: Boolean) {
        val deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        val params = mapOf(
            "userId" to userId,
            "deviceId" to deviceId,
            "isActive" to isActive
        )
        
        apiClient.post(ApiConfig.UPDATE_ACTIVE_STATUS, params, onSuccess = {}, onError = {})
    }

    private fun loadUserProfilePicture() {
        val url = "${ApiConfig.GET_USER}?userId=$userId"
        
        apiClient.get(
            url,
            onSuccess = { response ->
                try {
                    if (response.optBoolean("success", false) && response.has("data")) {
                        val user = response.getJSONObject("data").getJSONObject("user")
                        val profilePic = user.optString("profilePicBase64", "")
                        
                        // Save to SharedPreferences for future use
                        getSharedPreferences("user_session", MODE_PRIVATE).edit()
                            .putString("profilePic", profilePic)
                            .apply()
                        
                        // Update bottom nav profile picture
                        if (profilePic.isNotEmpty() && profilePic != "null") {
                            try {
                                val cleanBase64 = profilePic.replace("data:image/jpeg;base64,", "")
                                    .replace("data:image/png;base64,", "")
                                val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                navProfile.setImageBitmap(bitmap)
                            } catch (e: Exception) {
                                navProfile.setImageResource(R.drawable.pfp_image)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Silently fail
                }
            },
            onError = { /* Silently fail */ }
        )
    }

    private fun loadPosts() {
        // Use get_feed.php to get posts from followed users only
        val url = apiClient.buildUrlWithParams(ApiConfig.GET_FEED, mapOf(
            "userId" to userId.toString(),
            "limit" to "20",
            "offset" to "0"
        ))
        
        apiClient.get(url, onSuccess = { response ->
            try {
                // PHP API returns posts directly at root level (no "data" wrapper)
                val postsArray = response.getJSONArray("posts")
                
                feedItems.clear()
                for (i in 0 until postsArray.length()) {
                    try {
                        val post = postsArray.getJSONObject(i)
                        feedItems.add(
                            FeedPost(
                                postId = post.getInt("id"),
                                mediaBase64 = post.getString("mediaBase64"),
                                username = post.getString("username"),
                                caption = post.optString("caption", ""),
                                likes = post.getInt("likes"),
                                timestamp = post.getLong("timestamp"),
                                userId = post.getInt("userId"),
                                commentsCount = post.optInt("commentsCount", 0),
                                isLiked = post.optBoolean("isLiked", false),
                                profilePicBase64 = post.optString("profilePicBase64", "")
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error loading feed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, onError = { error ->
            // Silently fail during polling
        })
    }

    private fun logOutUser() {
        apiClient.post(
            ApiConfig.LOGOUT,
            mapOf("userId" to userId.toString()),
            onSuccess = { 
                getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                val i = Intent(this, page4::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                finish()
            },
            onError = {
                Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show()
            }
        )
    }

}

data class FeedPost(
    val postId: Int = 0,
    val mediaBase64: String = "",
    val username: String = "",
    val caption: String = "",
    var likes: Int = 0,
    val timestamp: Long = 0,
    val userId: Int = 0,
    var commentsCount: Int = 0,
    var isLiked: Boolean = false,
    val profilePicBase64: String = ""
)
