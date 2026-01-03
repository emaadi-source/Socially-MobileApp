package com.faujipanda.i230665_i230026

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Bottom Navigation Helper
 * Handles bottom navigation setup across all activities
 */
object BottomNavHelper {
    
    /**
     * Setup bottom navigation for an activity
     * @param activity The activity to setup navigation for
     * @param currentPage The current page identifier (to disable current button)
     */
    fun setupBottomNav(
        activity: Activity,
        currentPage: String,
        navHome: ImageView,
        navSearch: ImageView,
        navCreate: ImageView,
        navLike: ImageView,
        navProfile: CircleImageView
    ) {
        val shared = activity.getSharedPreferences("user_session", Activity.MODE_PRIVATE)
        val userId = shared.getInt("userId", 0)
        val email = shared.getString("lastEmail", "")
        val username = shared.getString("lastUsername", "")
        val profilePic = shared.getString("profilePic", "")
        
        Log.d("BottomNavHelper", "Loading profile pic. Length: ${profilePic?.length ?: 0}")
        
        // Load profile picture
        loadProfilePicture(navProfile, profilePic)
        
        // Home button
        navHome.setOnClickListener {
            if (currentPage != "home") {
                navigateToPage(activity, page5::class.java, userId, email, username)
            } else {
                Toast.makeText(activity, "Already on Home", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Search button
        navSearch.setOnClickListener {
            if (currentPage != "search") {
                navigateToPage(activity, page6::class.java, userId, email, username)
            } else {
                Toast.makeText(activity, "Already on Search", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Create button
        navCreate.setOnClickListener {
            navigateToPage(activity, CreatePostActivity::class.java, userId, email, username)
        }
        
        // Notifications/Likes button
        navLike.setOnClickListener {
            if (currentPage != "notifications") {
                navigateToPage(activity, page11::class.java, userId, email, username)
            } else {
                Toast.makeText(activity, "Already on Notifications", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Profile button
        navProfile.setOnClickListener {
            if (currentPage != "profile") {
                navigateToPage(activity, page13::class.java, userId, email, username)
            } else {
                Toast.makeText(activity, "Already on Profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Load profile picture into CircleImageView
     */
    private fun loadProfilePicture(imageView: CircleImageView, profilePicBase64: String?) {
        if (!profilePicBase64.isNullOrEmpty() && profilePicBase64 != "null") {
            try {
                Log.d("BottomNavHelper", "Attempting to load profile picture")
                val cleanBase64 = profilePicBase64.replace("data:image/jpeg;base64,", "")
                    .replace("data:image/png;base64,", "")
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bitmap)
                Log.d("BottomNavHelper", "Profile picture loaded successfully")
            } catch (e: Exception) {
                Log.e("BottomNavHelper", "Error loading profile picture", e)
                imageView.setImageResource(R.drawable.pfp_image)
            }
        } else {
            Log.d("BottomNavHelper", "No profile picture found, using default")
            imageView.setImageResource(R.drawable.pfp_image)
        }
    }
    
    /**
     * Navigate to a page with user data
     */
    private fun navigateToPage(
        activity: Activity,
        targetClass: Class<*>,
        userId: Int,
        email: String?,
        username: String?
    ) {
        val intent = Intent(activity, targetClass)
        intent.putExtra("userId", userId)
        intent.putExtra("email", email)
        intent.putExtra("username", username)
        activity.startActivity(intent)
    }
}
