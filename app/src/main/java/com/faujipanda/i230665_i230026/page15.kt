package com.faujipanda.i230665_i230026

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Edit Profile activity
class page15 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page15)
        Toast.makeText(this, "Edit Profile - implement with update_user.php API", Toast.LENGTH_SHORT).show()
    }
}