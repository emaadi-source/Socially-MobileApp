package com.faujipanda.i230665_i230026

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.InputStream

class EditProfileActivity : AppCompatActivity() {
    
    private var userId: Int = 0
    private var imageUri: Uri? = null
    private var currentProfilePic: String? = null
    
    private lateinit var apiClient: ApiClient
    private lateinit var ivProfilePic: CircleImageView
    private lateinit var btnChangePhoto: Button
    private lateinit var etUsername: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etBio: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: ImageView
    
    private val PICK_IMAGE_REQUEST = 1002
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        
        apiClient = ApiClient(this)
        
        // Get userId
        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = intent.getIntExtra("userId", shared.getInt("userId", 0))
        
        // Initialize views
        ivProfilePic = findViewById(R.id.ivProfilePic)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        etUsername = findViewById(R.id.etUsername)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etBio = findViewById(R.id.etBio)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        
        // Load current user data
        loadCurrentData()
        
        btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        
        btnSave.setOnClickListener {
            saveProfile()
        }
        
        btnCancel.setOnClickListener {
            finish()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            ivProfilePic.setImageURI(imageUri)
        }
    }
    
    private fun loadCurrentData() {
        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        currentProfilePic = shared.getString("profilePic", "")
        
        // Load profile picture
        if (!currentProfilePic.isNullOrEmpty() && currentProfilePic != "null") {
            try {
                val cleanBase64 = currentProfilePic!!.replace("data:image/jpeg;base64,", "")
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ivProfilePic.setImageBitmap(bitmap)
            } catch (e: Exception) {
                ivProfilePic.setImageResource(R.drawable.pfp_image)
            }
        }
        
        // Load user data from API
        val url = "${ApiConfig.GET_USER}?userId=$userId"
        
        apiClient.get(
            url,
            onSuccess = { response ->
                try {
                    val success = response.optBoolean("success", false)
                    if (success && response.has("data")) {
                        val user = response.getJSONObject("data").getJSONObject("user")
                        
                        etUsername.setText(user.getString("username"))
                        etFirstName.setText(user.getString("firstName"))
                        etLastName.setText(user.getString("lastName"))
                        etBio.setText(user.optString("bio", ""))
                        
                        // Update SharedPreferences with latest data
                        shared.edit()
                            .putString("lastUsername", user.getString("username"))
                            .putString("firstName", user.getString("firstName"))
                            .putString("lastName", user.getString("lastName"))
                            .apply()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    private fun saveProfile() {
        btnSave.isEnabled = false
        btnSave.text = "Saving..."
        
        val username = etUsername.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val bio = etBio.text.toString().trim()
        
        if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            btnSave.isEnabled = true
            btnSave.text = "Save"
            return
        }
        
        // Prepare data
        val params = mutableMapOf<String, Any>(
            "userId" to userId,
            "username" to username,
            "firstName" to firstName,
            "lastName" to lastName,
            "bio" to bio
        )
        
        // Handle profile picture if changed
        if (imageUri != null) {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                val baos = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, baos)
                val imageBytes = baos.toByteArray()
                val imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                
                params["profilePicBase64"] = imageBase64
            } catch (e: Exception) {
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = true
                btnSave.text = "Save"
                return
            }
        }
        
        // Update profile
        apiClient.post(
            ApiConfig.UPDATE_USER,
            params,
            onSuccess = { response ->
                try {
                    val user = response.getJSONObject("data").getJSONObject("user")
                    
                    // Update SharedPreferences
                    getSharedPreferences("user_session", MODE_PRIVATE).edit()
                        .putString("lastUsername", user.getString("username"))
                        .putString("firstName", user.getString("firstName"))
                        .putString("lastName", user.getString("lastName"))
                        .putString("profilePic", user.optString("profilePicBase64", ""))
                        .apply()
                    
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                    
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSave.isEnabled = true
                    btnSave.text = "Save"
                }
            },
            onError = { error ->
                Toast.makeText(this, "Failed to update: $error", Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = true
                btnSave.text = "Save"
            }
        )
    }
}
