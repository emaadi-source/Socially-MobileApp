package com.faujipanda.i230665_i230026

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine

abstract class CallActivity : AppCompatActivity() {

    protected var mRtcEngine: RtcEngine? = null
    protected var channelName: String = ""
    protected var isAudioMuted = false
    protected var isVideoMuted = false

    // REPLACE WITH YOUR AGORA APP ID
    private val appId = "ded3a611a88e46f2ae8c24426cdf467c" 

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d("Agora", "Remote user joined: $uid")
            runOnUiThread { 
                Toast.makeText(this@CallActivity, "User connected", Toast.LENGTH_SHORT).show()
                onRemoteUserJoined(uid) 
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d("Agora", "Remote user offline: $uid, reason: $reason")
            runOnUiThread { onRemoteUserOffline(uid) }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d("Agora", "Join channel SUCCESS: $channel, my uid: $uid")
            runOnUiThread { 
                Toast.makeText(this@CallActivity, "Connected to channel", Toast.LENGTH_SHORT).show()
            }
        }
        
        override fun onError(err: Int) {
            Log.e("Agora", "Agora error: $err")
            runOnUiThread {
                Toast.makeText(this@CallActivity, "Agora error: $err", Toast.LENGTH_LONG).show()
            }
        }
        
        override fun onConnectionStateChanged(state: Int, reason: Int) {
            Log.d("Agora", "Connection state changed: state=$state, reason=$reason")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelName = intent.getStringExtra("channelName") ?: ""
        Log.d("Agora", "CallActivity created with channel: $channelName")
        
        if (channelName.isEmpty()) {
            Toast.makeText(this, "Error: No channel name provided", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    protected fun initAgoraEngineAndJoinChannel(isVideo: Boolean) {
        Log.d("Agora", "Initializing Agora for channel: $channelName, isVideo: $isVideo")
        if (checkPermissions(isVideo)) {
            initializeAgoraEngine(isVideo)
            joinChannel()
        } else {
            Log.w("Agora", "Permissions not granted, requesting...")
            requestPermissions(isVideo)
        }
    }

    private fun initializeAgoraEngine(isVideo: Boolean) {
        try {
            mRtcEngine = RtcEngine.create(baseContext, appId, mRtcEventHandler)
            
            // Set channel profile to COMMUNICATION for 1-on-1 calls
            mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            
            if (isVideo) {
                // Enable video
                mRtcEngine?.enableVideo()
                // Set video encoder configuration
                mRtcEngine?.setVideoEncoderConfiguration(
                    io.agora.rtc2.video.VideoEncoderConfiguration(
                        io.agora.rtc2.video.VideoEncoderConfiguration.VD_640x360,
                        io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                        io.agora.rtc2.video.VideoEncoderConfiguration.STANDARD_BITRATE,
                        io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                    )
                )
            } else {
                // Disable video for voice calls
                mRtcEngine?.disableVideo()
            }
            
            // Always enable audio
            mRtcEngine?.enableAudio()
            
            // Set audio profile for better quality
            mRtcEngine?.setAudioProfile(
                Constants.AUDIO_PROFILE_DEFAULT,
                Constants.AUDIO_SCENARIO_DEFAULT
            )
            
        } catch (e: Exception) {
            Log.e("Agora", Log.getStackTraceString(e))
            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
    }

    private fun joinChannel() {
        // Set client role to BROADCASTER (both send and receive)
        mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        
        Log.d("Agora", "Fetching token for channel: $channelName")
        
        // Get token from server
        val url = "${ApiConfig.GENERATE_AGORA_TOKEN}?channelName=$channelName&uid=0"
        
        ApiClient(this).get(url, onSuccess = { response ->
            val success = response.optBoolean("success", false)
            if (success) {
                val token = response.optString("token")
                Log.d("Agora", "Got token, joining channel: $channelName")
                
                // Join with token
                val result = mRtcEngine?.joinChannel(token, channelName, "Extra Optional Data", 0)
                Log.d("Agora", "joinChannel result: $result")
            } else {
                val message = response.optString("message", "Unknown error")
                Log.e("Agora", "Failed to get token: $message")
                runOnUiThread {
                    Toast.makeText(this, "Failed to get token: $message", Toast.LENGTH_LONG).show()
                }
            }
        }, onError = { error ->
            Log.e("Agora", "Token request error: $error")
            runOnUiThread {
                Toast.makeText(this, "Network error: $error", Toast.LENGTH_LONG).show()
            }
        })
    }

    protected fun leaveChannel() {
        mRtcEngine?.leaveChannel()
    }

    protected open fun onRemoteUserJoined(uid: Int) {
        // Override in subclasses
    }

    protected open fun onRemoteUserOffline(uid: Int) {
        // Override in subclasses
        finish() // End call if other user leaves? Or just show status
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }
    
    protected fun toggleMic() {
        isAudioMuted = !isAudioMuted
        mRtcEngine?.muteLocalAudioStream(isAudioMuted)
    }

    private fun checkPermissions(isVideo: Boolean): Boolean {
        val permissions = if (isVideo) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
        
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions(isVideo: Boolean) {
        val permissions = if (isVideo) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
        ActivityCompat.requestPermissions(this, permissions, 22)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 22) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, initialize engine (need to know if video or not, maybe store state)
                // For simplicity, user has to click call again or we handle it better.
                // But for now, just toast.
                Toast.makeText(this, "Permissions granted. Please try calling again.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Permissions required for call.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
