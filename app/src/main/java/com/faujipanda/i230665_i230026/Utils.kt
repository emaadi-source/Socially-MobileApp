package com.faujipanda.i230665_i230026

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

/**
 * Generates a consistent unique device identifier.
 * Works across Android versions (does NOT require permissions).
 */
fun getDeviceIdentifier(context: Context): String {
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    return if (androidId != null) {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(androidId.toByteArray(Charsets.UTF_8))
        hash.joinToString("") { "%02x".format(it) }  // hex representation
    } else {
        "UNKNOWN_DEVICE"
    }
}
