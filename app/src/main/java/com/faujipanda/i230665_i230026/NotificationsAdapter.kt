package com.faujipanda.i230665_i230026

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class NotificationsAdapter(
    private val notifications: MutableList<Notification>,
    private val onAccept: (Notification) -> Unit,
    private val onReject: (Notification) -> Unit,
    private val onFollowBack: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: CircleImageView = view.findViewById(R.id.profilePic)
        val message: TextView = view.findViewById(R.id.message)
        val timestamp: TextView = view.findViewById(R.id.timestamp)
        val actionButtons: LinearLayout = view.findViewById(R.id.actionButtons)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnReject: Button = view.findViewById(R.id.btnReject)
        val btnFollowBack: Button = view.findViewById(R.id.btnFollowBack)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        
        // Load profile picture
        if (notification.sender.profilePicBase64.isNotEmpty()) {
            try {
                val cleanBase64 = notification.sender.profilePicBase64.replace("data:image/jpeg;base64,", "")
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.profilePic.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.profilePic.setImageResource(R.drawable.pfp_image)
            }
        } else {
            holder.profilePic.setImageResource(R.drawable.pfp_image)
        }
        
        // Set message based on type
        val senderName = notification.sender.username
        when (notification.type) {
            "follow_request" -> {
                holder.message.text = "$senderName requested to follow you"
                holder.actionButtons.visibility = View.VISIBLE
                holder.btnAccept.visibility = View.VISIBLE
                holder.btnReject.visibility = View.VISIBLE
                holder.btnFollowBack.visibility = View.GONE
                
                holder.btnAccept.setOnClickListener { onAccept(notification) }
                holder.btnReject.setOnClickListener { onReject(notification) }
            }
            "follow_accept" -> {
                holder.message.text = "You started following $senderName"
                holder.actionButtons.visibility = View.GONE
            }
            "follow_back" -> {
                holder.message.text = "$senderName started following you"
                holder.actionButtons.visibility = View.VISIBLE
                holder.btnAccept.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
                holder.btnFollowBack.visibility = View.VISIBLE
                holder.btnFollowBack.text = "Follow Back"
                
                holder.btnFollowBack.setOnClickListener { onFollowBack(notification) }
            }
            "like" -> {
                val likeCount = notification.post?.likes ?: 0
                holder.message.text = if (likeCount > 1) {
                    "$senderName and ${likeCount - 1} others liked your post"
                } else {
                    "$senderName liked your post"
                }
                holder.actionButtons.visibility = View.GONE
            }
            "comment" -> {
                val commentCount = notification.post?.comments ?: 0
                holder.message.text = if (commentCount > 1) {
                    "$senderName and ${commentCount - 1} others commented on your post"
                } else {
                    "$senderName commented on your post"
                }
                holder.actionButtons.visibility = View.GONE
            }
        }
        
        // Format timestamp
        val timeAgo = getTimeAgo(notification.timestamp)
        holder.timestamp.text = timeAgo
    }
    
    override fun getItemCount() = notifications.size
    
    fun updateNotifications(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }
    
    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> "${diff / 86400000}d ago"
        }
    }
}
