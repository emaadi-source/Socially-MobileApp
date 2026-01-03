package com.faujipanda.i230665_i230026

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray

class CommentsActivity : AppCompatActivity() {

    private lateinit var apiClient: ApiClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var sendBtn: ImageView
    private lateinit var adapter: CommentsAdapter
    private val commentsList = mutableListOf<CommentItem>()

    private var postId: Int = 0
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        apiClient = ApiClient(this)

        recyclerView = findViewById(R.id.recyclerComments)
        commentInput = findViewById(R.id.commentInput)
        sendBtn = findViewById(R.id.sendBtn)

        recyclerView.layoutManager = LinearLayoutManager(this) // Standard layout (oldest at top, newest at bottom)
        adapter = CommentsAdapter(commentsList)
        recyclerView.adapter = adapter

        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        postId = intent.getIntExtra("postId", 0)
        userId = intent.getIntExtra("userId", shared.getInt("userId", 0))

        if (postId == 0 || userId == 0) {
            Toast.makeText(this, "Invalid post or session", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadComments()
        sendBtn.setOnClickListener { postComment() }

        val close = findViewById<TextView>(R.id.close)
        close.setOnClickListener {
            finish()
        }
    }

    private fun loadComments() {
        val url = apiClient.buildUrlWithParams(
            ApiConfig.GET_COMMENTS,
            mapOf("postId" to postId.toString())
        )

        apiClient.get(url,
            onSuccess = { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        // PHP API returns comments directly at root level (merged by sendSuccess)
                        val commentsArray = response.getJSONArray("comments")
                        commentsList.clear()
                        for (i in 0 until commentsArray.length()) {
                            val comment = commentsArray.getJSONObject(i)
                            commentsList.add(
                                CommentItem(
                                    username = comment.getString("username"),
                                    text = comment.getString("text"), // Fixed: comment_text -> text
                                    timestamp = comment.getLong("timestamp"), // Fixed: created_at -> timestamp
                                    profilePic = comment.optString("profilePicBase64", "") // Fixed: profile_pic_base64 -> profilePicBase64
                                )
                            )
                        }
                        adapter.notifyDataSetChanged()
                        // Scroll to bottom to show latest comments
                        if (commentsList.isNotEmpty()) {
                            recyclerView.scrollToPosition(commentsList.size - 1)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun postComment() {
        val text = commentInput.text.toString().trim()
        if (TextUtils.isEmpty(text)) return

        apiClient.post(
            ApiConfig.ADD_COMMENT,
            mapOf(
                "postId" to postId.toString(),
                "userId" to userId.toString(),
                "text" to text
            ),
            onSuccess = { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        commentInput.text.clear()
                        loadComments() // Reload to show new comment
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

data class CommentItem(
    val username: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val profilePic: String = ""
)

class CommentsAdapter(private val comments: List<CommentItem>) :
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: CircleImageView = view.findViewById(R.id.commentAvatar)
        val username: TextView = view.findViewById(R.id.commentUsername)
        val text: TextView = view.findViewById(R.id.commentText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(v)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.username.text = comment.username
        holder.text.text = comment.text

        if (comment.profilePic.isNotEmpty()) {
            try {
                val cleanBase64 = comment.profilePic.replace("data:image/jpeg;base64,", "")
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.avatar.setImageBitmap(bmp)
            } catch (e: Exception) {
                holder.avatar.setImageResource(R.drawable.pfp_image)
            }
        } else {
            holder.avatar.setImageResource(R.drawable.pfp_image)
        }
    }

    override fun getItemCount() = comments.size
}
