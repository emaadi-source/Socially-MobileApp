package com.faujipanda.i230665_i230026

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class page16 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page16)

        var btncacel=findViewById<TextView>(R.id.btnCancel)
        btncacel.setOnClickListener {
            startActivity(Intent(this, page5::class.java))
        }
//variable creation
        var btnnext=findViewById<TextView>(R.id.btnNext)
        btnnext.setOnClickListener {
            startActivity(Intent(this, page5::class.java))
        }

    }
}