package com.faujipanda.i230665_i230026

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class page4 : AppCompatActivity() {

    private lateinit var apiClient: ApiClient
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignup: TextView
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page4)

        apiClient = ApiClient(this)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLoginP4)
        tvSignup = findViewById(R.id.tvSignup)
        ivBack = findViewById(R.id.btnBack)

        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        
        // Pre-fill username from intent (from page3) or last session
        val prefillUsername = intent.getStringExtra("prefillUsername")
        val lastUsername = shared.getString("lastUsername", null)
        
        if (!prefillUsername.isNullOrEmpty()) {
            etUsername.setText(prefillUsername)
        } else if (!lastUsername.isNullOrEmpty()) {
            etUsername.setText(lastUsername)
        }

        ivBack.setOnClickListener {
            startActivity(Intent(this, page3::class.java))
            finish()
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, page2::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username and password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password, shared)
        }
    }

    private fun performLogin(username: String, password: String, shared: android.content.SharedPreferences) {
        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        
        apiClient.post(
            ApiConfig.LOGIN,
            mapOf(
                "username" to username,
                "password" to password,
                "deviceId" to deviceId
            ),
            onSuccess = { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val user = response.getJSONObject("user")
                        val userId = user.getInt("id")
                        val email = user.getString("email")
                        val actualUsername = user.getString("username")
                        val firstName = user.optString("first_name", "")
                        val lastName = user.optString("last_name", "")
                        val profilePic = user.optString("profile_pic_base64", "")

                        // Save session locally
                        shared.edit()
                            .putInt("userId", userId)
                            .putString("lastEmail", email)
                            .putString("lastUsername", actualUsername)
                            .putString("firstName", firstName)
                            .putString("lastName", lastName)
                            .putString("profilePic", profilePic)
                            .putBoolean("isLoggedIn", true)
                            .putBoolean("hasEverLoggedIn", true)
                            .apply()

                        // Create session on server
                        createSession(userId, deviceId)

                        // Navigate to home
                        val i = Intent(this, page5::class.java)
                        i.putExtra("userId", userId)
                        i.putExtra("email", email)
                        i.putExtra("username", actualUsername)
                        startActivity(i)
                        finish()
                    } else {
                        val message = response.optString("message", "Login failed")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        btnLogin.isEnabled = true
                        btnLogin.text = "Login"
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing response: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                }
            },
            onError = { error ->
                Toast.makeText(this, "Login failed: $error", Toast.LENGTH_SHORT).show()
                btnLogin.isEnabled = true
                btnLogin.text = "Login"
            }
        )
    }

    private fun createSession(userId: Int, deviceId: String) {
        apiClient.post(
            ApiConfig.CREATE_SESSION,
            mapOf(
                "userId" to userId.toString(),
                "deviceId" to deviceId
            ),
            onSuccess = { response ->
                // Session created successfully
                val sessionId = response.optString("sessionId", "")
                getSharedPreferences("user_session", MODE_PRIVATE)
                    .edit()
                    .putString("sessionId", sessionId)
                    .apply()
            },
            onError = { error ->
                // Session creation failed, but continue anyway
                android.util.Log.e("page4", "Failed to create session: $error")
            }
        )
    }
}
