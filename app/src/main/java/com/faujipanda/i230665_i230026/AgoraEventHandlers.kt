package com.faujipanda.i230665_i230026

import io.agora.rtc2.IRtcEngineEventHandler


object AgoraEventHandlers {
    fun defaultHandler(
        onRemoteJoined: (Int) -> Unit = {},
        onRemoteLeft: (Int) -> Unit = {}
    ): IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            onRemoteJoined(uid)
        }
        override fun onUserOffline(uid: Int, reason: Int) {
            onRemoteLeft(uid)
        }
    }
}
