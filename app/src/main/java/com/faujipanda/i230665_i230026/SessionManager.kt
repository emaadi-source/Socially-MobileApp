package com.faujipanda.i230665_i230026

import android.app.Application
import android.provider.Settings

class SessionManager : Application() {
    
    private var currentUserId: Int = 0
    private var sessionId: String = ""
    
    fun initSession(email: String, sessionId: String) {
        this.sessionId = sessionId
        // Session initialized
    }
    
    fun getUserId(): Int {
        return currentUserId
    }
    
    fun setUserId(userId: Int) {
        this.currentUserId = userId
    }
}