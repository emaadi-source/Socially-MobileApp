package com.faujipanda.i230665_i230026

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Chat activity
class page9 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page9)
        Toast.makeText(this, "Chat feature - implement with send_message.php API", Toast.LENGTH_SHORT).show()
    }
}