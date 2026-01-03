package com.faujipanda.i230665_i230026

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class page13 : AppCompatActivity() {
    
    private var userId: Int = 0
    private var email: String? = null
    private var username: String? = null
    
    private lateinit var apiClient: ApiClient
    private lateinit var adapter: UserPostsAdapter
    private val posts = mutableListOf<UserPost>()
    
    // UI elements
    private lateinit var tvUser: TextView
    private lateinit var tvName: TextView
    private lateinit var tvBio: TextView
    private lateinit var ivAvatar: CircleImageView
    private lateinit var postCount: TextView
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView
    private lateinit var editProfile: TextView
    private lateinit var recyclerView: RecyclerView
    
    // Bottom nav
    private lateinit var navHome: ImageView
    private lateinit var navSearch: ImageView
    private lateinit var navCreate: ImageView
    private lateinit var navLike: ImageView
    private lateinit var navProfile: CircleImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page13)
        
        apiClient = ApiClient(this)
        
        // Get user data from intent or SharedPreferences
        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = intent.getIntExtra("userId", shared.getInt("userId", 0))
        email = intent.getStringExtra("email") ?: shared.getString("lastEmail", "")
        username = intent.getStringExtra("username") ?: shared.getString("lastUsername", "")
        
        Log.d("page13", "UserId: $userId, Email: $email, Username: $username")
        
        // Initialize views
        tvUser = findViewById(R.id.tvUser)
        tvName = findViewById(R.id.tvName)
        tvBio = findViewById(R.id.tvBio1)
        ivAvatar = findViewById(R.id.ivAvatar)
        postCount = findViewById(R.id.postCount)
        followersCount = findViewById(R.id.followersCount)
        followingCount = findViewById(R.id.followingCount)
        editProfile = findViewById(R.id.editprofile)
        recyclerView = findViewById(R.id.recyclerView)
        
        // Bottom navigation
        navHome = findViewById(R.id.navHome)
        navSearch = findViewById(R.id.navSearch)
        navCreate = findViewById(R.id.navCreate)
        navLike = findViewById(R.id.navLike)
        navProfile = findViewById(R.id.navProfile)
        
        // Setup bottom navigation
        BottomNavHelper.setupBottomNav(
            this,
            "profile",
            navHome,
            navSearch,
            navCreate,
            navLike,
            navProfile
        )
        
        // Setup RecyclerView with 3 columns
        adapter = UserPostsAdapter(posts)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
        
        // Edit profile button
        editProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
        
        // Load user data
        loadUserProfile()
        loadUserPosts()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload data when returning from edit profile
        loadUserProfile()
        loadUserPosts()
    }
    
    private fun loadUserProfile() {
        val url = "${ApiConfig.GET_USER}?userId=$userId"
        
        Log.d("page13", "Loading profile from: $url")
        
        apiClient.get(
            url,
            onSuccess = { response ->
                try {
                    Log.d("page13", "Profile response: $response")
                    
                    val success = response.optBoolean("success", false)
                    if (!success) {
                        val message = response.optString("message", "Unknown error")
                        Toast.makeText(this, "Server error: $message", Toast.LENGTH_LONG).show()
                        return@get
                    }
                    
                    // User data is under "data.user"
                    if (!response.has("data")) {
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_LONG).show()
                        return@get
                    }
                    
                    val data = response.getJSONObject("data")
                    if (!data.has("user")) {
                        Toast.makeText(this, "User data not found in response", Toast.LENGTH_LONG).show()
                        return@get
                    }
                    
                    val user = data.getJSONObject("user")
                    
                    // Update UI with user data
                    val usernameText = user.getString("username")
                    val firstName = user.getString("firstName")
                    val lastName = user.getString("lastName")
                    val bio = user.optString("bio", "")
                    val profilePic = user.optString("profilePicBase64", "")
                    val posts = user.getInt("postsCount")
                    val followers = user.getInt("followersCount")
                    val following = user.getInt("followingCount")
                    
                    tvUser.text = usernameText
                    tvName.text = "$firstName $lastName"
                    tvBio.text = if (bio.isEmpty()) "No bio yet" else bio
                    postCount.text = posts.toString()
                    followersCount.text = followers.toString()
                    followingCount.text = following.toString()
                    
                    // Load profile picture
                    if (profilePic.isNotEmpty() && profilePic != "null") {
                        try {
                            val cleanBase64 = profilePic.replace("data:image/jpeg;base64,", "")
                            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            ivAvatar.setImageBitmap(bitmap)
                            navProfile.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            Log.e("page13", "Error loading profile pic", e)
                            ivAvatar.setImageResource(R.drawable.pfp_image)
                            navProfile.setImageResource(R.drawable.pfp_image)
                        }
                    } else {
                        ivAvatar.setImageResource(R.drawable.pfp_image)
                        navProfile.setImageResource(R.drawable.pfp_image)
                    }
                    
                } catch (e: Exception) {
                    Log.e("page13", "Error parsing profile", e)
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            onError = { error ->
                Log.e("page13", "Profile load error: $error")
                Toast.makeText(this, "Failed to load profile: $error", Toast.LENGTH_LONG).show()
            }
        )
    }
    
    private fun loadUserPosts() {
        val url = "${ApiConfig.GET_POSTS}?targetUserId=$userId"
        
        Log.d("page13", "Loading posts from: $url")
        
        apiClient.get(
            url,
            onSuccess = { response ->
                try {
                    Log.d("page13", "Posts response: $response")
                    
                    val success = response.optBoolean("success", false)
                    if (!success) {
                        val message = response.optString("message", "Unknown error")
                        Toast.makeText(this, "Server error: $message", Toast.LENGTH_LONG).show()
                        return@get
                    }
                    
                    if (!response.has("data")) {
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_LONG).show()
                        return@get
                    }
                    
                    val data = response.getJSONObject("data")
                    val postsArray = data.getJSONArray("posts")
                    val userPosts = mutableListOf<UserPost>()
                    
                    Log.d("page13", "Found ${postsArray.length()} posts")
                    
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
                    
                } catch (e: Exception) {
                    Log.e("page13", "Error parsing posts", e)
                    Toast.makeText(this, "Error loading posts: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            onError = { error ->
                Log.e("page13", "Posts load error: $error")
                Toast.makeText(this, "Failed to load posts: $error", Toast.LENGTH_LONG).show()
            }
        )
    }
}
