package com.faujipanda.i230665_i230026

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas

class page10 : AppCompatActivity() {

    private var agoraEngine: RtcEngine? = null
    private val appId = "YOUR_AGORA_APP_ID" // Replace with your Agora App ID
    private var channelName: String = ""
    private var role: String = "caller"
    private var isAudioMuted = false
    private var isVideoMuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page10)

        channelName = intent.getStringExtra("channelName") ?: "test-channel"
        role = intent.getStringExtra("role") ?: "caller"

        setupAgoraEngine()
        joinChannel()
    }

    private fun setupAgoraEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = applicationContext
            config.mAppId = appId
            config.mEventHandler = object : IRtcEngineEventHandler() {
                override fun onUserJoined(uid: Int, elapsed: Int) {
                    runOnUiThread {
                        setupRemoteVideo(uid)
                    }
                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    runOnUiThread {
                        Toast.makeText(this@page10, "User left", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    runOnUiThread {
                        Toast.makeText(this@page10, "Joined channel", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            agoraEngine = RtcEngine.create(config)
            agoraEngine?.enableVideo()
            setupLocalVideo()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Agora initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupLocalVideo() {
        try {
            val surfaceView = RtcEngine.CreateRendererView(baseContext)
            surfaceView.setZOrderMediaOverlay(true)

            val videoCanvas = VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0)
            agoraEngine?.setupLocalVideo(videoCanvas)
            agoraEngine?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        try {
            val surfaceView = RtcEngine.CreateRendererView(baseContext)
            val videoCanvas = VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid)
            agoraEngine?.setupRemoteVideo(videoCanvas)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun joinChannel() {
        val options = ChannelMediaOptions()
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

        agoraEngine?.joinChannel(null, channelName, 0, options)
    }

    private fun toggleAudio() {
        isAudioMuted = !isAudioMuted
        agoraEngine?.muteLocalAudioStream(isAudioMuted)
    }

    private fun toggleVideo() {
        isVideoMuted = !isVideoMuted
        agoraEngine?.muteLocalVideoStream(isVideoMuted)
    }

    private fun endCall() {
        agoraEngine?.leaveChannel()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine?.stopPreview()
        agoraEngine?.leaveChannel()
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }
}
