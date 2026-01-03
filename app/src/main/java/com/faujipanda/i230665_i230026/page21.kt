package com.faujipanda.i230665_i230026

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Other User Profile activity
class page21 : AppCompatActivity() {
    private lateinit var apiClient: ApiClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page21)
        
        apiClient = ApiClient(this)
        Toast.makeText(this, "Other user profile - implement with get_user.php API", Toast.LENGTH_SHORT).show()
    }
}
