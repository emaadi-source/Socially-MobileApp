package com.faujipanda.i230665_i230026

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CreatePostActivity : AppCompatActivity() {

    private lateinit var apiClient: ApiClient
    private lateinit var offlineRepository: OfflineRepository
    private var userId: Int = 0
    private var imageUri: Uri? = null

    private lateinit var imgPreview: ImageView
    private lateinit var etCaption: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var btnUpload: Button
    private lateinit var btnCancel: TextView

    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        apiClient = ApiClient(this)
        offlineRepository = OfflineRepository(this)
        
        // Get user data from intent or SharedPreferences
        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = intent.getIntExtra("userId", shared.getInt("userId", 0))
        val email = intent.getStringExtra("email") ?: shared.getString("lastEmail", "")
        val username = intent.getStringExtra("username") ?: shared.getString("lastUsername", "")

        if (userId == 0) {
            Toast.makeText(this, "Session lost. Please log in again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        imgPreview = findViewById(R.id.imgPreview)
        etCaption = findViewById(R.id.etCaption)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnUpload = findViewById(R.id.btnUpload)
        btnCancel = findViewById(R.id.btnCancel)

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnUpload.setOnClickListener {
            if (imageUri == null) {
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show()
            } else {
                uploadPost()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imgPreview.setImageURI(imageUri)
        }
    }

    private fun uploadPost() {
        btnUpload.isEnabled = false
        btnUpload.text = "Uploading..."
        
        try {
            val caption = etCaption.text.toString().trim()
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Compress & encode to Base64
            val baos = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, baos)
            val imageBytes = baos.toByteArray()
            val imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            // Use OfflineRepository for offline-first post creation
            offlineRepository.createPost(
                userId = userId,
                mediaBase64 = imageBase64,
                mediaType = "image",
                caption = caption
            ) { success, postId, message ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "Upload failed: $message", Toast.LENGTH_SHORT).show()
                        btnUpload.isEnabled = true
                        btnUpload.text = "Upload Post"
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            btnUpload.isEnabled = true
            btnUpload.text = "Upload Post"
        }
    }
}