package com.faujipanda.i230665_i230026

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ViewStoryActivity : AppCompatActivity() {

    private lateinit var apiClient: ApiClient
    private lateinit var storyImage: ImageView
    private lateinit var storyVideo: VideoView
    private lateinit var username: TextView
    private lateinit var progressBar: ProgressBar
    
    private var userId: Int = 0
    private val stories = mutableListOf<StoryItem>()
    private var currentIndex = 0
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_story)

        apiClient = ApiClient(this)
        userId = intent.getIntExtra("userId", 0)

        storyImage = findViewById(R.id.storyImage)
        storyVideo = findViewById(R.id.storyVideo)
        username = findViewById(R.id.storyUsername)
        progressBar = findViewById(R.id.storyProgress)

        if (userId == 0) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Tap to navigate
        val touchListener = android.view.View.OnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val width = v.width
                if (event.x < width / 2) {
                    showPreviousStory()
                } else {
                    showNextStory()
                }
            }
            true
        }
        
        storyImage.setOnTouchListener(touchListener)
        storyVideo.setOnTouchListener(touchListener)

        loadUserStories()
    }

    private fun loadUserStories() {
        val url = apiClient.buildUrlWithParams(
            ApiConfig.GET_USER_STORIES,
            mapOf("userId" to userId.toString())
        )

        apiClient.get(url,
            onSuccess = { response ->
                try {
                    val storiesArray = response.getJSONArray("stories")
                    stories.clear()
                    for (i in 0 until storiesArray.length()) {
                        val s = storiesArray.getJSONObject(i)
                        stories.add(
                            StoryItem(
                                id = s.getInt("id"),
                                mediaBase64 = s.getString("mediaBase64"),
                                mediaType = s.getString("mediaType"),
                                username = s.getString("username")
                            )
                        )
                    }
                    
                    if (stories.isNotEmpty()) {
                        currentIndex = 0
                        showStory(currentIndex)
                    } else {
                        Toast.makeText(this, "No active stories", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error loading stories", Toast.LENGTH_SHORT).show()
                    finish()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Failed to load stories", Toast.LENGTH_SHORT).show()
                finish()
            }
        )
    }
    
    private fun showStory(index: Int) {
        if (index < 0 || index >= stories.size) {
            finish()
            return
        }
        
        val story = stories[index]
        username.text = story.username
        
        // Reset views
        storyImage.visibility = android.view.View.GONE
        storyVideo.visibility = android.view.View.GONE
        handler.removeCallbacksAndMessages(null)
        
        try {
            val cleanBase64 = story.mediaBase64.replace("data:image/jpeg;base64,", "")
                .replace("data:image/png;base64,", "")
                .replace("data:video/mp4;base64,", "")
                
            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            
            if (story.mediaType == "video") {
                storyVideo.visibility = android.view.View.VISIBLE
                
                // Save to temp file to play
                val tempFile = java.io.File.createTempFile("story_video", ".mp4", cacheDir)
                val fos = java.io.FileOutputStream(tempFile)
                fos.write(bytes)
                fos.close()
                
                storyVideo.setVideoPath(tempFile.absolutePath)
                storyVideo.start()
                
                storyVideo.setOnCompletionListener {
                    showNextStory()
                }
                
            } else {
                storyImage.visibility = android.view.View.VISIBLE
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                storyImage.setImageBitmap(bmp)
                
                // Auto advance after 5 seconds
                handler.postDelayed({
                    showNextStory()
                }, 5000)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Try to load placeholder with Picasso if decoding fails
            com.squareup.picasso.Picasso.get().load(R.drawable.pfp_image).into(storyImage)
            // But still advance to next story after delay so user isn't stuck
             handler.postDelayed({
                showNextStory()
            }, 2000)
        }
    }
    
    private fun showNextStory() {
        currentIndex++
        if (currentIndex < stories.size) {
            showStory(currentIndex)
        } else {
            finish()
        }
    }
    
    private fun showPreviousStory() {
        if (currentIndex > 0) {
            currentIndex--
            showStory(currentIndex)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}

data class StoryItem(
    val id: Int,
    val mediaBase64: String,
    val mediaType: String,
    val username: String
)
