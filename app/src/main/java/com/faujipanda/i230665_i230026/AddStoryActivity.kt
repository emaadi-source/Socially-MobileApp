package com.faujipanda.i230665_i230026

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AddStoryActivity : AppCompatActivity() {

    private lateinit var apiClient: ApiClient
    private lateinit var offlineRepository: OfflineRepository
    private lateinit var preview: ImageView
    private lateinit var btnSelect: Button
    private lateinit var btnUpload: Button
    private var selectedUri: Uri? = null
    private var selectedBase64: String? = null
    private var mediaType = "image"
    private var userId: Int = 0

    private val PICK_MEDIA = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        apiClient = ApiClient(this)
        offlineRepository = OfflineRepository(this)

        preview = findViewById(R.id.storyPreview)
        btnSelect = findViewById(R.id.btnSelectStory)
        btnUpload = findViewById(R.id.btnUploadStory)

        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = intent.getIntExtra("userId", shared.getInt("userId", 0))

        if (userId == 0) {
            Toast.makeText(this, "Session lost! Please re-login.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnSelect.setOnClickListener {
            val pick = Intent(Intent.ACTION_PICK)
            pick.type = "*/*"
            pick.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            startActivityForResult(pick, PICK_MEDIA)
        }

        btnUpload.setOnClickListener {
            if (selectedBase64 == null) {
                Toast.makeText(this, "Select a media first", Toast.LENGTH_SHORT).show()
            } else {
                uploadStory(selectedBase64!!, mediaType)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MEDIA && resultCode == Activity.RESULT_OK && data?.data != null) {
            val uri = data.data!!
            selectedUri = uri

            val type = contentResolver.getType(uri) ?: ""
            mediaType = if (type.startsWith("video")) "video" else "image"

            if (mediaType == "video") {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(this, uri)

                    val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                    if (durationMs > 10_000) {
                        Toast.makeText(this, "Video must be 10 seconds or less", Toast.LENGTH_SHORT).show()
                        retriever.release()
                        selectedUri = null
                        return
                    }

                    val thumb = retriever.getFrameAtTime(0)
                    retriever.release()
                    preview.setImageBitmap(thumb)

                    val input = contentResolver.openInputStream(uri)
                    selectedBase64 = encodeToBase64(input)
                    Toast.makeText(this, "Video ready to upload", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error reading video", Toast.LENGTH_SHORT).show()
                }

            } else {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                preview.setImageBitmap(bitmap)
                selectedBase64 = bitmapToBase64(bitmap)
                Toast.makeText(this, "Image ready to upload", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        return "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    private fun encodeToBase64(input: InputStream?): String? {
        if (input == null) return null
        return try {
            val buffer = ByteArrayOutputStream()
            input.copyTo(buffer)
            input.close()
            "data:video/mp4;base64," + Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadStory(base64: String, type: String) {
        btnUpload.isEnabled = false
        btnUpload.text = "Uploading..."

        offlineRepository.createStory(
            userId = userId,
            mediaBase64 = base64,
            mediaType = type,
            onComplete = { success, storyId, message ->
                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        finish()
                    } else {
                        btnUpload.isEnabled = true
                        btnUpload.text = "Upload Story"
                    }
                }
            }
        )
    }
}
