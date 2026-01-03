package com.faujipanda.i230665_i230026

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(
    private val context: Context,
    private val messages: List<Map<String, Any>>,
    private val currentUserId: Int,
    private val onAction: (Map<String, Any>, String) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_SENT) R.layout.item_message_right else R.layout.item_message_left
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemViewType(position: Int): Int {
        val senderIdObj = messages[position]["senderId"]
        val senderId = when (senderIdObj) {
            is Int -> senderIdObj
            is Long -> senderIdObj.toInt()
            is String -> senderIdObj.toIntOrNull() ?: 0
            else -> 0
        }
        return if (senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun getItemCount() = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMessage: TextView = itemView.findViewById(R.id.messageText)
        private val imgMessage: ImageView = itemView.findViewById(R.id.messageImage)
        // private val txtTime: TextView = itemView.findViewById(R.id.txtTime) // Not in layout

        fun bind(message: Map<String, Any>) {
            val text = message["text"] as String
            val mediaBase64 = message["mediaBase64"] as String
            val timestamp = message["timestamp"] as Long
            val isEdited = (message["isEdited"] as? Int) == 1

            if (text.isNotEmpty()) {
                txtMessage.visibility = View.VISIBLE
                txtMessage.text = if (isEdited) "$text (edited)" else text
            } else {
                txtMessage.visibility = View.GONE
            }

            if (mediaBase64.isNotEmpty()) {
                imgMessage.visibility = View.VISIBLE
                try {
                    val cleanBase64 = mediaBase64.replace("data:image/jpeg;base64,", "")
                        .replace("data:image/png;base64,", "")
                    val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    imgMessage.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    imgMessage.visibility = View.GONE
                }
            } else {
                imgMessage.visibility = View.GONE
            }

            // Double tap listener
            // Double tap listener
            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val senderIdObj = message["senderId"]
                    val senderId = when (senderIdObj) {
                        is Int -> senderIdObj
                        is Long -> senderIdObj.toInt()
                        is String -> senderIdObj.toIntOrNull() ?: 0
                        else -> 0
                    }
                    
                    if (senderId == currentUserId) {
                        onAction(message, "edit") 
                        return true
                    }
                    return super.onDoubleTap(e)
                }
                
                override fun onLongPress(e: MotionEvent) {
                     val senderIdObj = message["senderId"]
                    val senderId = when (senderIdObj) {
                        is Int -> senderIdObj
                        is Long -> senderIdObj.toInt()
                        is String -> senderIdObj.toIntOrNull() ?: 0
                        else -> 0
                    }
                    
                    if (senderId == currentUserId) {
                        onAction(message, "delete")
                    }
                }
            })

            itemView.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
        }
    }
}
