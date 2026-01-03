package com.faujipanda.i230665_i230026

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for displaying user posts in a grid layout (3 columns)
 */
class UserPostsAdapter(
    private val posts: MutableList<UserPost>
) : RecyclerView.Adapter<UserPostsAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postImage: ImageView = view.findViewById(R.id.postImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        
        // Load image from base64
        try {
            val cleanBase64 = post.mediaBase64.replace("data:image/jpeg;base64,", "")
            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            holder.postImage.setImageBitmap(bitmap)
        } catch (e: Exception) {
            holder.postImage.setImageResource(R.drawable.pfp_image)
        }
    }

    override fun getItemCount() = posts.size
    
    fun updatePosts(newPosts: List<UserPost>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }
}

data class UserPost(
    val postId: String,
    val mediaBase64: String,
    val caption: String,
    val likes: Int,
    val commentsCount: Int
)
