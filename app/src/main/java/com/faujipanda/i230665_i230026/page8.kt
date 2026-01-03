package com.faujipanda.i230665_i230026

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class page8 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var repository: OfflineRepository
    private lateinit var adapter: MutualFollowsAdapter
    private val users = mutableListOf<Map<String, Any>>()
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page8)

        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = intent.getIntExtra("userId", shared.getInt("userId", 0))

        repository = OfflineRepository(this)

        // Use correct ID from activity_page8.xml
        recyclerView = findViewById(R.id.recyclerDM)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MutualFollowsAdapter(users) { user ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("otherUserId", user["id"] as Int)
            intent.putExtra("otherUsername", user["username"] as String)
            intent.putExtra("otherProfilePic", user["profilePicBase64"] as String)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadMutualFollows()
    }

    private fun loadMutualFollows() {
        repository.getMutualFollows(userId) { mutualUsers ->
            runOnUiThread {
                users.clear()
                users.addAll(mutualUsers)
                adapter.notifyDataSetChanged()
                
                if (users.isEmpty()) {
                    Toast.makeText(this, "No mutual follows found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class MutualFollowsAdapter(
        private val users: List<Map<String, Any>>,
        private val onClick: (Map<String, Any>) -> Unit
    ) : RecyclerView.Adapter<MutualFollowsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val profilePic: CircleImageView = view.findViewById(R.id.ivProfilePic)
            val username: TextView = view.findViewById(R.id.tvUsername)
            val fullName: TextView = view.findViewById(R.id.tvFullName)

            init {
                view.setOnClickListener { onClick(users[adapterPosition]) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_search, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]
            holder.username.text = user["username"] as String
            holder.fullName.text = "" // We don't have full name in the map currently, or we can fetch it
            
            val profilePicBase64 = user["profilePicBase64"] as String
            if (profilePicBase64.isNotEmpty()) {
                try {
                    val cleanBase64 = profilePicBase64.replace("data:image/jpeg;base64,", "")
                        .replace("data:image/png;base64,", "")
                    val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    holder.profilePic.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    holder.profilePic.setImageResource(R.drawable.pfp_image)
                }
            } else {
                holder.profilePic.setImageResource(R.drawable.pfp_image)
            }
        }

        override fun getItemCount() = users.size
    }
}
