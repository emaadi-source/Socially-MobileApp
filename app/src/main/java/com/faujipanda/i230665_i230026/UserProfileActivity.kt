package com.faujipanda.i230665_i230026

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

class UserProfileActivity : AppCompatActivity() {
    
    private var userId: Int = 0
    private var targetUserId: Int = 0
    private lateinit var apiClient: ApiClient
    
    private lateinit var profilePic: CircleImageView
    private lateinit var username: TextView
    private lateinit var fullName: TextView
    private lateinit var bio: TextView
    private lateinit var postsCount: TextView
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView
    private lateinit var btnFollow: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserPostsAdapter
    
    private var followStatus = "none" // none, pending, following
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        
        apiClient = ApiClient(this)
        
        userId = intent.getIntExtra("userId", 0)
        targetUserId = intent.getIntExtra("targetUserId", 0)
        
        // Initialize views
        profilePic = findViewById(R.id.ivAvatar)
        username = findViewById(R.id.tvUser)
        fullName = findViewById(R.id.tvName)
        bio = findViewById(R.id.tvBio1)
        postsCount = findViewById(R.id.postCount)
        followersCount = findViewById(R.id.followersCount)
        followingCount = findViewById(R.id.followingCount)
        btnFollow = findViewById(R.id.btnFollow)
        recyclerView = findViewById(R.id.recyclerView)
        
        // Setup RecyclerView
        adapter = UserPostsAdapter(mutableListOf())
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
        
        // Load user data
        loadUserProfile()
        loadUserPosts()
        checkFollowStatus()
        
        // Follow button click
        btnFollow.setOnClickListener {
            handleFollowAction()
        }
    }
    
    override fun onResume() {
        super.onResume()
        startPolling()
    }
    
    override fun onPause() {
        super.onPause()
        stopPolling()
    }
    
    private val pollingHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val pollingRunnable = object : Runnable {
        override fun run() {
            loadUserProfile()
            checkFollowStatus()
            pollingHandler.postDelayed(this, 5000) // Poll every 5 seconds
        }
    }
    
    private fun startPolling() {
        pollingHandler.postDelayed(pollingRunnable, 5000)
    }
    
    private fun stopPolling() {
        pollingHandler.removeCallbacks(pollingRunnable)
    }
    
    private fun loadUserProfile() {
        val url = apiClient.buildUrlWithParams(ApiConfig.GET_USER, mapOf(
            "userId" to targetUserId.toString()
        ))
        
        apiClient.get(url, onSuccess = { response ->
            val data = response.getJSONObject("data")
            val user = data.getJSONObject("user")
            
            username.text = user.getString("username")
            fullName.text = "${user.getString("firstName")} ${user.getString("lastName")}"
            bio.text = user.optString("bio", "")
            postsCount.text = user.getInt("postsCount").toString()
            followersCount.text = user.getInt("followersCount").toString()
            followingCount.text = user.getInt("followingCount").toString()
            
            // Load profile picture
            val profilePicBase64 = user.optString("profilePicBase64", "")
            if (profilePicBase64.isNotEmpty()) {
                try {
                    val cleanBase64 = profilePicBase64.replace("data:image/jpeg;base64,", "")
                    val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    profilePic.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    profilePic.setImageResource(R.drawable.pfp_image)
                }
            }
        }, onError = { error ->
            Toast.makeText(this, "Failed to load profile: $error", Toast.LENGTH_SHORT).show()
        })
    }
    
    private fun loadUserPosts() {
        val url = apiClient.buildUrlWithParams(ApiConfig.GET_POSTS, mapOf(
            "targetUserId" to targetUserId.toString()
        ))
        
        apiClient.get(url, onSuccess = { response ->
            val data = response.getJSONObject("data")
            val postsArray = data.getJSONArray("posts")
            val userPosts = mutableListOf<UserPost>()
            
            for (i in 0 until postsArray.length()) {
                val post = postsArray.getJSONObject(i)
                userPosts.add(
                    UserPost(
                        postId = post.getString("postId"),
                        mediaBase64 = post.getString("mediaBase64"),
                        caption = post.optString("caption", ""),
                        likes = post.getInt("likes"),
                        commentsCount = post.getInt("commentsCount")
                    )
                )
            }
            
            adapter.updatePosts(userPosts)
        }, onError = { error ->
            Toast.makeText(this, "Failed to load posts: $error", Toast.LENGTH_SHORT).show()
        })
    }
    
    private fun checkFollowStatus() {
        val url = apiClient.buildUrlWithParams(ApiConfig.CHECK_FOLLOWING, mapOf(
            "userId" to userId.toString(),
            "targetUserId" to targetUserId.toString()
        ))
        
        apiClient.get(url, onSuccess = { response ->
            val data = response.getJSONObject("data")
            followStatus = data.getString("status")
            updateFollowButton()
        }, onError = {})
    }
    
    private fun updateFollowButton() {
        when (followStatus) {
            "none" -> {
                btnFollow.text = "Follow"
                btnFollow.setBackgroundResource(R.drawable.greyish_button)
            }
            "pending" -> {
                btnFollow.text = "Requested"
                btnFollow.setBackgroundResource(R.drawable.greyish_button)
            }
            "following" -> {
                btnFollow.text = "Following"
                btnFollow.setBackgroundResource(R.drawable.greyish_button)
            }
        }
    }
    
    private fun handleFollowAction() {
        when (followStatus) {
            "none" -> sendFollowRequest()
            "pending" -> cancelFollowRequest()
            "following" -> unfollowUser()
        }
    }
    
    private fun sendFollowRequest() {
        val params = mapOf(
            "userId" to userId,
            "targetUserId" to targetUserId
        )
        
        apiClient.post(ApiConfig.FOLLOW_USER, params, onSuccess = {
            followStatus = "pending"
            updateFollowButton()
            Toast.makeText(this, "Follow request sent", Toast.LENGTH_SHORT).show()
        }, onError = { error ->
            Toast.makeText(this, "Failed to send request: $error", Toast.LENGTH_SHORT).show()
        })
    }
    
    private fun cancelFollowRequest() {
        val params = mapOf(
            "userId" to userId,
            "targetUserId" to targetUserId
        )
        
        apiClient.post(ApiConfig.UNFOLLOW_USER, params, onSuccess = {
            followStatus = "none"
            updateFollowButton()
            Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show()
        }, onError = { error ->
            Toast.makeText(this, "Failed to cancel request: $error", Toast.LENGTH_SHORT).show()
        })
    }
    
    private fun unfollowUser() {
        val params = mapOf(
            "userId" to userId,
            "targetUserId" to targetUserId
        )
        
        apiClient.post(ApiConfig.UNFOLLOW_USER, params, onSuccess = {
            followStatus = "none"
            updateFollowButton()
            Toast.makeText(this, "Unfollowed", Toast.LENGTH_SHORT).show()
            // Reload profile to update follower count
            loadUserProfile()
        }, onError = { error ->
            Toast.makeText(this, "Failed to unfollow: $error", Toast.LENGTH_SHORT).show()
        })
    }
}
