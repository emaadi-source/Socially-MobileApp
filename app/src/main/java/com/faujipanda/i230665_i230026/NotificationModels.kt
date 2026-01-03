package com.faujipanda.i230665_i230026

data class Notification(
    val id: Int,
    val type: String,
    val postId: Int?,
    val isRead: Boolean,
    val timestamp: Long,
    val sender: NotificationSender,
    val post: NotificationPost? = null
)

data class NotificationSender(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val profilePicBase64: String
)

data class NotificationPost(
    val mediaBase64: String,
    val likes: Int,
    val comments: Int
)
