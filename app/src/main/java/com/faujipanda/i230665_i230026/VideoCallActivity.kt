package com.faujipanda.i230665_i230026

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import io.agora.rtc2.Constants
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.RtcEngine

class VideoCallActivity : CallActivity() {

    private lateinit var remoteVideoContainer: FrameLayout
    private lateinit var localVideoContainer: FrameLayout
    private lateinit var btnSwitchCamera: ImageView
    private lateinit var btnMute: ImageView
    private lateinit var btnVideoToggle: ImageView
    private lateinit var btnEndCall: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        remoteVideoContainer = findViewById(R.id.remoteVideoContainer)
        localVideoContainer = findViewById(R.id.localVideoContainer)
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera)
        btnMute = findViewById(R.id.btnMute)
        btnVideoToggle = findViewById(R.id.btnVideoToggle)
        btnEndCall = findViewById(R.id.btnEndCall)

        btnSwitchCamera.setOnClickListener {
            mRtcEngine?.switchCamera()
        }

        btnMute.setOnClickListener {
            toggleMic()
            btnMute.alpha = if (isAudioMuted) 0.5f else 1.0f
        }

        btnVideoToggle.setOnClickListener {
            isVideoMuted = !isVideoMuted
            mRtcEngine?.muteLocalVideoStream(isVideoMuted)
            localVideoContainer.visibility = if (isVideoMuted) View.GONE else View.VISIBLE
            btnVideoToggle.alpha = if (isVideoMuted) 0.5f else 1.0f
        }

        btnEndCall.setOnClickListener {
            leaveChannel()
            finish()
        }

        initAgoraEngineAndJoinChannel(true)
        setupLocalVideo()
    }

    private fun setupLocalVideo() {
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        localVideoContainer.addView(surfaceView)
        mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    override fun onRemoteUserJoined(uid: Int) {
        runOnUiThread {
            setupRemoteVideo(uid)
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        if (remoteVideoContainer.childCount > 0) {
            return
        }
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        remoteVideoContainer.addView(surfaceView)
        mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    override fun onRemoteUserOffline(uid: Int) {
        runOnUiThread {
            remoteVideoContainer.removeAllViews()
            finish() // End call if remote user leaves
        }
    }
}
