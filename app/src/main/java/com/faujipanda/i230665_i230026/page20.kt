package com.faujipanda.i230665_i230026

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class page20 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page20)

        var create=findViewById<LinearLayout>(R.id.create)
        create.setOnClickListener {
            startActivity(Intent(this, page19::class.java))
        }

        var btnClose=findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener {
            startActivity(Intent(this,page5::class.java))
        }
    }
}