package com.faujipanda.i230665_i230026

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Token update is handled in MainActivity/Page4 usually
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val data = remoteMessage.data
        val type = data["type"]
        
        if (type == "call_incoming") {
            val intent = Intent(this, CallIncomingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("channelName", data["channelName"])
            intent.putExtra("callerName", data["callerName"])
            intent.putExtra("callerPic", data["callerPic"])
            intent.putExtra("callType", data["callType"])
            startActivity(intent)
        }
    }
}