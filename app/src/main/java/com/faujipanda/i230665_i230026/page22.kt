package com.faujipanda.i230665_i230026

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Followers/Following list activity
class page22 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page22)
        Toast.makeText(this, "Followers list - implement with get_followers.php API", Toast.LENGTH_SHORT).show()
    }
}
