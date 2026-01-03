package com.faujipanda.i230665_i230026

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class SearchResultsAdapter(
    private val users: MutableList<SearchUser>,
    private val onUserClick: (SearchUser) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: CircleImageView = view.findViewById(R.id.profilePic)
        val username: TextView = view.findViewById(R.id.username)
        val fullName: TextView = view.findViewById(R.id.fullName)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        
        holder.username.text = user.username
        holder.fullName.text = "${user.firstName} ${user.lastName}"
        
        // Load profile picture
        if (user.profilePicBase64.isNotEmpty()) {
            try {
                val cleanBase64 = user.profilePicBase64.replace("data:image/jpeg;base64,", "")
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.profilePic.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.profilePic.setImageResource(R.drawable.pfp_image)
            }
        } else {
            holder.profilePic.setImageResource(R.drawable.pfp_image)
        }
        
        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }
    
    override fun getItemCount() = users.size
    
    fun updateResults(newUsers: List<SearchUser>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}
