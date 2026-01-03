package com.faujipanda.i230665_i230026

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

data class ChatPreview(
    val chatId: String = "",
    val otherUserEmail: String = "",
    val otherUsername: String = "",
    val profilePicBase64: String? = null,
    val lastMessage: String = "",
    val online: Boolean = false,
    val lastMsgTime: Long = 0L
)

class DMListAdapter(
    private var chatList: List<ChatPreview>,
    private val currentUserKey: String
) : RecyclerView.Adapter<DMListAdapter.ViewHolder>() {

    fun updateData(newList: List<ChatPreview>) {
        chatList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: CircleImageView = view.findViewById(R.id.dmAvatar)
        val name: TextView = view.findViewById(R.id.dmName)
        val message: TextView = view.findViewById(R.id.dmPreview)
        val onlineDot: ImageView = view.findViewById(R.id.onlineDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position]
        holder.name.text = chat.otherUsername.ifEmpty { "Unknown User" }
        holder.message.text = if (chat.lastMessage.isNotEmpty()) chat.lastMessage else "Say hi 👋"

        // Online indicator
        holder.onlineDot.setImageResource(
            if (chat.online) R.drawable.online_dot else R.drawable.offline_dot
        )

        // Decode Base64 → set profile picture
        if (!chat.profilePicBase64.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(chat.profilePicBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.avatar.setImageBitmap(bmp)
            } catch (_: Exception) {
                holder.avatar.setImageResource(R.drawable.pfp_image)
            }
        } else {
            holder.avatar.setImageResource(R.drawable.pfp_image)
        }

        // Click → open chat screen
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val i = Intent(context, page9::class.java)
            i.putExtra("chatId", chat.chatId)
            i.putExtra("receiverEmail", chat.otherUserEmail)
            i.putExtra("receiverName", chat.otherUsername)
            i.putExtra("receiverProfile", chat.profilePicBase64)
            i.putExtra("senderKey", currentUserKey)
            context.startActivity(i)
        }
    }

    override fun getItemCount() = chatList.size
}
