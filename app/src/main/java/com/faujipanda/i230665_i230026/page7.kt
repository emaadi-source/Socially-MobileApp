package com.faujipanda.i230665_i230026

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// User list/followers activity
class page7 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page7)
        Toast.makeText(this, "Followers feature - implement with get_followers.php API", Toast.LENGTH_SHORT).show()
    }
}
