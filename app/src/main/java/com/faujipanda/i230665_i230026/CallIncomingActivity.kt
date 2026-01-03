package com.faujipanda.i230665_i230026

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.hdodenhof.circleimageview.CircleImageView

class CallIncomingActivity : AppCompatActivity() {

    private var channelName: String = ""
    private var callerName: String = ""
    private var callerPic: String = ""
    private var callType: String = ""
    private var callId: Int = 0
    private lateinit var apiClient: ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_incoming)

        apiClient = ApiClient(this)
        
        channelName = intent.getStringExtra("channelName") ?: ""
        callerName = intent.getStringExtra("callerName") ?: "Unknown"
        callerPic = intent.getStringExtra("callerPic") ?: ""
        callType = intent.getStringExtra("callType") ?: "voice"
        callId = intent.getIntExtra("callId", 0)

        val tvCaller = findViewById<TextView>(R.id.tvCaller)
        val btnAccept = findViewById<Button>(R.id.btnAccept)
        val btnDecline = findViewById<Button>(R.id.btnDecline)

        tvCaller.text = "Incoming $callType call from $callerName"

        btnAccept.setOnClickListener {
            respondToCall("accept")
            val intent = if (callType == "voice") {
                Intent(this, VoiceCallActivity::class.java)
            } else {
                Intent(this, VideoCallActivity::class.java)
            }
            intent.putExtra("channelName", channelName)
            intent.putExtra("otherUsername", callerName)
            intent.putExtra("otherProfilePic", callerPic)
            startActivity(intent)
            finish()
        }

        btnDecline.setOnClickListener {
            respondToCall("reject")
            finish()
        }
    }
    
    private fun respondToCall(action: String) {
        val params = mapOf(
            "callId" to callId,
            "action" to action
        )
        apiClient.post(ApiConfig.RESPOND_TO_CALL, params, onSuccess = {}, onError = {})
    }
}