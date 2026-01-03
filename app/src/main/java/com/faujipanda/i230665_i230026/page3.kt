package com.faujipanda.i230665_i230026

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.hdodenhof.circleimageview.CircleImageView

class page3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page3)
        
        val username = intent.getStringExtra("username") ?: ""
        val profilePicBase64 = intent.getStringExtra("profilePicBase64") ?: ""
        val hasAccount = username.isNotEmpty()
        
        val profilePic = findViewById<CircleImageView>(R.id.profilePic)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSwitchAccount = findViewById<Button>(R.id.btnSwitchAccount)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        
        if (hasAccount) {
            // Has previous account
            welcomeText.text = "Welcome back, $username"
            btnLogin.isEnabled = true
            btnLogin.alpha = 1.0f
            
            // Load profile picture
            if (profilePicBase64.isNotEmpty()) {
                try {
                    val cleanBase64 = profilePicBase64.replace("data:image/jpeg;base64,", "")
                    val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    profilePic.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    profilePic.setImageResource(R.drawable.pfp_image)
                }
            }
        } else {
            // No account ever logged in
            welcomeText.text = "No Account"
            profilePic.setImageResource(R.drawable.pfp_image)
            btnLogin.isEnabled = false
            btnLogin.alpha = 0.5f
        }
        
        btnLogin.setOnClickListener {
            if (hasAccount) {
                val intent = Intent(this, page4::class.java)
                intent.putExtra("prefillUsername", username)
                startActivity(intent)
                finish()
            }
        }
        
        btnSwitchAccount.setOnClickListener {
            startActivity(Intent(this, page4::class.java))
            finish()
        }
        
        btnBack.setOnClickListener {
            startActivity(Intent(this, page2::class.java))
            finish()
        }
    }
}