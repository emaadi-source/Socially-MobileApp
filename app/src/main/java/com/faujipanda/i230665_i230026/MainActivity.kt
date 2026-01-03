package com.faujipanda.i230665_i230026

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Get device ID
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        
        // Wait 5 seconds then check session
        Handler(Looper.getMainLooper()).postDelayed({
            checkSessionAndRoute(deviceId)
        }, 5000)
    }
    
    private fun checkSessionAndRoute(deviceId: String) {
        val apiClient = ApiClient(this)
        val url = apiClient.buildUrlWithParams(ApiConfig.CHECK_SESSION_STATUS, mapOf(
            "deviceId" to deviceId
        ))
        
        apiClient.get(url, onSuccess = { response ->
            try {
                val data = response.getJSONObject("data")
                val hasSession = data.getBoolean("hasSession")
                val isLoggedIn = data.getBoolean("isLoggedIn")
                
                when {
                    // Device has a session (ever logged in) - always go to page3
                    hasSession -> {
                        val username = data.getString("username")
                        val profilePic = data.optString("profilePicBase64", "")
                        
                        // If currently logged in, go directly to feed
                        if (isLoggedIn) {
                            val userId = data.getInt("userId")
                            val email = data.getString("email")
                            
                            val intent = Intent(this, page5::class.java)
                            intent.putExtra("userId", userId)
                            intent.putExtra("username", username)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        } else {
                            // Not logged in but has session - go to page3 (lock screen)
                            val intent = Intent(this, page3::class.java)
                            intent.putExtra("username", username)
                            intent.putExtra("profilePic", profilePic)
                            startActivity(intent)
                        }
                        finish()
                    }
                    else -> {
                        // No session - go to page2 (signup/login choice)
                        startActivity(Intent(this, page2::class.java))
                        finish()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to page2 on error
                startActivity(Intent(this, page2::class.java))
                finish()
            }
        }, onError = { error ->
            // On error, default to registration
            startActivity(Intent(this, page2::class.java))
            finish()
        })
    }
}
