package com.faujipanda.i230665_i230026

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import android.os.Handler
import android.os.Looper
import com.squareup.picasso.Picasso

class FeedWithStoriesAdapter(
    private val posts: List<FeedPost>,
    private val currentUserId: Int,
    private val apiClient: ApiClient
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_STORY = 0
    private val TYPE_POST = 1
    private var handler = Handler(Looper.getMainLooper())
    private var storyViewHolder: StoryHeaderViewHolder? = null

    override fun getItemCount() = posts.size + 1
    override fun getItemViewType(position: Int) = if (position == 0) TYPE_STORY else TYPE_POST

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_STORY) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_stories_header, parent, false)
            val holder = StoryHeaderViewHolder(v, apiClient)
            storyViewHolder = holder
            holder
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
            FeedViewHolder(v, currentUserId, apiClient)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is StoryHeaderViewHolder) holder.bind(currentUserId)
        else if (holder is FeedViewHolder) holder.bind(posts[position - 1])
    }

    fun refreshStories(userId: Int) {
        storyViewHolder?.bind(userId)
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ refreshStories(userId) }, 120000)
    }

    class StoryHeaderViewHolder(view: View, private val apiClient: ApiClient) : RecyclerView.ViewHolder(view) {
        private val container: LinearLayout = view.findViewById(R.id.storiesRow)

        fun bind(currentUserId: Int) {
            android.util.Log.d("StoriesAdapter", "Binding stories for user: $currentUserId")
            val url = apiClient.buildUrlWithParams(
                ApiConfig.GET_STORIES,
                mapOf("userId" to currentUserId.toString())
            )

            apiClient.get(url,
                onSuccess = { response ->
                    try {
                        android.util.Log.d("StoriesAdapter", "Stories response: $response")
                        container.removeAllViews()
                        val users = response.getJSONArray("users")
                        var currentUserHasStory = false
                        var currentUserProfilePic = ""

                        // Check if current user has a story
                        for (i in 0 until users.length()) {
                            val user = users.getJSONObject(i)
                            if (user.getInt("userId") == currentUserId) {
                                currentUserHasStory = true
                                currentUserProfilePic = user.optString("profilePicBase64", "")
                                break
                            }
                        }

                        // Add "Your Story" first
                        addYourStoryCircle(currentUserId, currentUserHasStory, currentUserProfilePic)

                        // Add other users
                        for (i in 0 until users.length()) {
                            val user = users.getJSONObject(i)
                            if (user.getInt("userId") != currentUserId) {
                                addStoryCircle(
                                    user.getInt("userId"),
                                    user.getString("username"),
                                    user.optString("profilePicBase64", "")
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.util.Log.e("StoriesAdapter", "Error parsing stories: ${e.message}")
                    }
                },
                onError = { error -> 
                    android.util.Log.e("StoriesAdapter", "Error fetching stories: $error")
                }
            )
        }

        private fun addYourStoryCircle(userId: Int, hasStory: Boolean, profilePic: String) {
            val context = itemView.context
            val storyView = LayoutInflater.from(context).inflate(R.layout.item_story_circle, container, false)
            val imageView = storyView.findViewById<CircleImageView>(R.id.storyImage)
            val nameView = storyView.findViewById<TextView>(R.id.storyUsername)

            // Load profile pic if available, otherwise default
            if (profilePic.isNotEmpty() && profilePic != "null") {
                try {
                    val cleanBase64 = profilePic.replace("data:image/jpeg;base64,", "")
                        .replace("data:image/png;base64,", "")
                    val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    imageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Picasso.get().load(R.drawable.pfp_image).into(imageView)
                }
            } else {
                Picasso.get().load(R.drawable.pfp_image).into(imageView)
            }

            nameView.text = "Your Story"

            storyView.setOnClickListener {
                if (hasStory) {
                    val intent = Intent(context, ViewStoryActivity::class.java)
                    intent.putExtra("userId", userId)
                    context.startActivity(intent)
                } else {
                    val intent = Intent(context, AddStoryActivity::class.java)
                    intent.putExtra("userId", userId)
                    context.startActivity(intent)
                }
            }

            container.addView(storyView)
        }

        private fun addStoryCircle(userId: Int, username: String, profilePic: String) {
            val context = itemView.context
            val storyView = LayoutInflater.from(context).inflate(R.layout.item_story_circle, container, false)
            val imageView = storyView.findViewById<CircleImageView>(R.id.storyImage)
            val nameView = storyView.findViewById<TextView>(R.id.storyUsername)

            if (profilePic.isNotEmpty() && profilePic != "null") {
                try {
                    val cleanBase64 = profilePic.replace("data:image/jpeg;base64,", "")
                        .replace("data:image/png;base64,", "")
                    val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    imageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Picasso.get().load(R.drawable.pfp_image).into(imageView)
                }
            } else {
                Picasso.get().load(R.drawable.pfp_image).into(imageView)
            }

            nameView.text = username

            storyView.setOnClickListener {
                val intent = Intent(context, ViewStoryActivity::class.java)
                intent.putExtra("userId", userId)
                context.startActivity(intent)
            }

            container.addView(storyView)
        }
    }

    class FeedViewHolder(view: View, private val currentUserId: Int, private val apiClient: ApiClient) : RecyclerView.ViewHolder(view) {
        private val avatar: CircleImageView = view.findViewById(R.id.feedAvatar)
        private val username: TextView = view.findViewById(R.id.feedUsername)
        private val postImage: ImageView = view.findViewById(R.id.feedImage)
        private val caption: TextView = view.findViewById(R.id.feedCaption)
        private val likeButton: ImageView = view.findViewById(R.id.feedHeart)
        private val commentButton: ImageView = view.findViewById(R.id.feedComment)
        private val likeCount: TextView = view.findViewById(R.id.feedLikes)
        private val commentCount: TextView = view.findViewById(R.id.feedComments)

        fun bind(post: FeedPost) {
            username.text = post.username
            caption.text = post.caption
            likeCount.text = "${post.likes} likes"
            commentCount.text = "${post.commentsCount} comments"

            // Load post image
            if (post.mediaBase64.isNotEmpty()) {
                try {
                    val cleanBase64 = post.mediaBase64.replace("data:image/jpeg;base64,", "")
                        .replace("data:image/png;base64,", "")
                    val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    postImage.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Load profile picture
            if (post.profilePicBase64.isNotEmpty() && post.profilePicBase64 != "null") {
                try {
                    val cleanPfp = post.profilePicBase64.replace("data:image/jpeg;base64,", "")
                        .replace("data:image/png;base64,", "")
                    val pfpBytes = Base64.decode(cleanPfp, Base64.DEFAULT)
                    val pfpBitmap = BitmapFactory.decodeByteArray(pfpBytes, 0, pfpBytes.size)
                    avatar.setImageBitmap(pfpBitmap)
                } catch (e: Exception) {
                    Picasso.get().load(R.drawable.pfp_image).into(avatar)
                }
            } else {
                Picasso.get().load(R.drawable.pfp_image).into(avatar)
            }

            // Set initial like button state
            if (post.isLiked) {
                likeButton.setImageResource(R.drawable.like_icon_filled)
            } else {
                likeButton.setImageResource(R.drawable.like_icon)
            }

            // Like button click
            likeButton.setOnClickListener {
                toggleLike(post)
            }

            // Comment button click
            commentButton.setOnClickListener {
                val intent = Intent(itemView.context, CommentsActivity::class.java)
                intent.putExtra("postId", post.postId)
                intent.putExtra("userId", currentUserId)
                itemView.context.startActivity(intent)
            }
        }

        private fun toggleLike(post: FeedPost) {
            val params = mapOf(
                "postId" to post.postId.toString(),
                "userId" to currentUserId.toString()
            )

            apiClient.post(ApiConfig.TOGGLE_LIKE, params, onSuccess = { response ->
                try {
                    val data = response.getJSONObject("data")
                    val liked = data.getBoolean("liked")
                    val newCount = data.getInt("likeCount")
                    
                    post.likes = newCount
                    post.isLiked = liked
                    likeCount.text = "$newCount likes"
                    
                    // Update like button appearance
                    if (liked) {
                        likeButton.setImageResource(R.drawable.like_icon_filled)
                    } else {
                        likeButton.setImageResource(R.drawable.like_icon)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(itemView.context, "Error updating like", Toast.LENGTH_SHORT).show()
                }
            }, onError = {
                Toast.makeText(itemView.context, "Failed to like post", Toast.LENGTH_SHORT).show()
            })
        }
    }
}
