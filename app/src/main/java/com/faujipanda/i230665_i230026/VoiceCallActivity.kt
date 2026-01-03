package com.faujipanda.i230665_i230026

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine

class VoiceCallActivity : CallActivity() {

    private lateinit var imgProfile: CircleImageView
    private lateinit var txtName: TextView
    private lateinit var txtStatus: TextView
    private lateinit var btnMute: ImageView
    private lateinit var btnEndCall: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_call)

        val otherUsername = intent.getStringExtra("otherUsername") ?: "Unknown"
        val otherProfilePic = intent.getStringExtra("otherProfilePic") ?: ""
        
        imgProfile = findViewById(R.id.imgProfile)
        txtName = findViewById(R.id.txtName)
        txtStatus = findViewById(R.id.txtStatus)
        btnMute = findViewById(R.id.btnMute)
        btnEndCall = findViewById(R.id.btnEndCall)

        txtName.text = otherUsername
        
        if (otherProfilePic.isNotEmpty()) {
            try {
                val cleanBase64 = otherProfilePic.replace("data:image/jpeg;base64,", "")
                    .replace("data:image/png;base64,", "")
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgProfile.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imgProfile.setImageResource(R.drawable.pfp_image)
            }
        }

        btnMute.setOnClickListener {
            toggleMic()
            btnMute.alpha = if (isAudioMuted) 0.5f else 1.0f
        }

        btnEndCall.setOnClickListener {
            leaveChannel()
            finish()
        }

        initAgoraEngineAndJoinChannel(false)
    }

    override fun onRemoteUserJoined(uid: Int) {
        runOnUiThread {
            txtStatus.text = "Connected"
        }
    }

    override fun onRemoteUserOffline(uid: Int) {
        runOnUiThread {
            txtStatus.text = "Call Ended"
            finish()
        }
    }
}
