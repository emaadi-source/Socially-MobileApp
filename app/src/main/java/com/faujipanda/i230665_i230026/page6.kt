package com.faujipanda.i230665_i230026

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

class page6 : AppCompatActivity() {
    
    private var userId: Int = 0
    private var email: String? = null
    private var username: String? = null
    
    private lateinit var apiClient: ApiClient
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchResultsAdapter
    
    private lateinit var navHome: ImageView
    private lateinit var navSearch: ImageView
    private lateinit var navCreate: ImageView
    private lateinit var navLike: ImageView
    private lateinit var navProfile: CircleImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page6)
        
        apiClient = ApiClient(this)
        
        // Get user data
        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = intent.getIntExtra("userId", shared.getInt("userId", 0))
        email = intent.getStringExtra("email") ?: shared.getString("lastEmail", "")
        username = intent.getStringExtra("username") ?: shared.getString("lastUsername", "")
        
        // Initialize views
        searchInput = findViewById(R.id.searchInput)
        recyclerView = findViewById(R.id.recyclerView)
        
        navHome = findViewById(R.id.navHome)
        navSearch = findViewById(R.id.navSearch)
        navCreate = findViewById(R.id.navCreate)
        navLike = findViewById(R.id.navLike)
        navProfile = findViewById(R.id.navProfile)
        
        // Setup RecyclerView
        adapter = SearchResultsAdapter(mutableListOf()) { user ->
            // Open user profile
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("targetUserId", user.id)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        // Setup search with debounce
        searchInput.addTextChangedListener(object : TextWatcher {
            private var searchRunnable: Runnable? = null
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                searchRunnable?.let { android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(it) }
                
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    adapter.updateResults(emptyList())
                    return
                }
                
                searchRunnable = Runnable {
                    searchUsers(query)
                }
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(searchRunnable!!, 300)
            }
        })
        
        // Setup bottom navigation
        BottomNavHelper.setupBottomNav(
            this,
            "search",
            navHome,
            navSearch,
            navCreate,
            navLike,
            navProfile
        )
        
        loadUserProfilePicture()
    }
    
    private fun searchUsers(query: String) {
        val url = apiClient.buildUrlWithParams(ApiConfig.SEARCH_USERS, mapOf(
            "query" to query,
            "userId" to userId.toString()
        ))
        
        apiClient.get(url, onSuccess = { response ->
            val data = response.getJSONObject("data")
            val usersArray = data.getJSONArray("users")
            val results = mutableListOf<SearchUser>()
            
            for (i in 0 until usersArray.length()) {
                val user = usersArray.getJSONObject(i)
                results.add(
                    SearchUser(
                        id = user.getInt("id"),
                        username = user.getString("username"),
                        firstName = user.getString("firstName"),
                        lastName = user.getString("lastName"),
                        profilePicBase64 = user.optString("profilePicBase64", "")
                    )
                )
            }
            
            adapter.updateResults(results)
        }, onError = { error ->
            Toast.makeText(this, "Search failed: $error", Toast.LENGTH_SHORT).show()
        })
    }
    
    private fun loadUserProfilePicture() {
        val url = apiClient.buildUrlWithParams(ApiConfig.GET_USER, mapOf("userId" to userId.toString()))
        apiClient.get(url, onSuccess = { response ->
            val data = response.getJSONObject("data")
            val user = data.getJSONObject("user")
            val profilePic = user.optString("profilePicBase64", "")
            
            if (profilePic.isNotEmpty()) {
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                    android.util.Base64.decode(profilePic.replace("data:image/jpeg;base64,", ""), android.util.Base64.DEFAULT),
                    0,
                    android.util.Base64.decode(profilePic.replace("data:image/jpeg;base64,", ""), android.util.Base64.DEFAULT).size
                )
                navProfile.setImageBitmap(bitmap)
            }
        }, onError = {})
    }
}

data class SearchUser(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val profilePicBase64: String
)
