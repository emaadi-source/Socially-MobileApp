package com.faujipanda.i230665_i230026

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class page18 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page18)

        var btnclose=findViewById<ImageView>(R.id.btnClose)
        btnclose.setOnClickListener {
            startActivity(Intent(this, page5::class.java))
        }
    }
}