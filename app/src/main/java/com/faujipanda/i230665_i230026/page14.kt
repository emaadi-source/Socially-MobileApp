package com.faujipanda.i230665_i230026

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class page14 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page14)

        var close=findViewById<ImageView>(R.id.btnClose)
        close.setOnClickListener {
            startActivity(Intent(this, page13::class.java))
        }
    }
}