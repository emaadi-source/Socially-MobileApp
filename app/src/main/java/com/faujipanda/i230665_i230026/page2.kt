package com.faujipanda.i230665_i230026

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Base64
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.util.*

class page2 : AppCompatActivity() {

    private lateinit var apiClient: ApiClient
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var username: EditText
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var dob: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var createAccountBtn: Button
    private lateinit var togglePassword: ImageView
    private lateinit var profilePic: ImageView
    private lateinit var navButton: ImageView

    private var profilePicUri: Uri? = null
    private var passwordVisible = false
    private var selectedDob: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page2)

        apiClient = ApiClient(this)

        username = findViewById(R.id.username)
        firstName = findViewById(R.id.firstName)
        lastName = findViewById(R.id.lastName)
        dob = findViewById(R.id.dob)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        createAccountBtn = findViewById(R.id.createAccountBtn)
        togglePassword = findViewById(R.id.togglePassword)
        profilePic = findViewById(R.id.profilePic)
        navButton = findViewById(R.id.backBtn)

        // 🖼 Image picker
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                profilePicUri = it.data!!.data
                profilePic.setImageURI(profilePicUri)
            }
        }

        profilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        // 👁 Toggle password visibility
        togglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.eye_starred)
            } else {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.eye_hidden)
            }
            password.setSelection(password.text.length)
        }

        // DOB picker
        dob.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(this, { _, y, m, d ->
                selectedDob = Calendar.getInstance().apply { set(y, m, d) }
                dob.setText(String.format("%02d-%02d-%04d", d, m + 1, y))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            dpd.datePicker.maxDate = c.timeInMillis
            dpd.show()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                createAccountBtn.isEnabled = allFilled()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        listOf(username, firstName, lastName, dob, email, password).forEach { it.addTextChangedListener(watcher) }

        createAccountBtn.setOnClickListener { validateAndCreate() }

        navButton.setOnClickListener {
            startActivity(Intent(this, page3::class.java))
            finish()
        }
    }

    private fun allFilled(): Boolean =
        username.text.isNotBlank() &&
                firstName.text.isNotBlank() &&
                lastName.text.isNotBlank() &&
                dob.text.isNotBlank() &&
                email.text.isNotBlank() &&
                password.text.isNotBlank()

    private fun validateAndCreate() {
        val uname = username.text.toString().trim()
        val fname = firstName.text.toString().trim()
        val lname = lastName.text.toString().trim()
        val date = dob.text.toString().trim()
        val mail = email.text.toString().trim()
        val pass = password.text.toString().trim()

        if (uname.contains(" ")) {
            toast("Username must not contain spaces"); return
        }
        if (profilePicUri == null) {
            toast("Please select a profile picture"); return
        }
        if (!is18OrOlder()) {
            toast("You must be at least 18 years old to create an account."); return
        }

        // Check username availability via API
        val checkUrl = apiClient.buildUrlWithParams(
            ApiConfig.CHECK_USERNAME,
            mapOf("username" to uname)
        )

        apiClient.get(checkUrl,
            onSuccess = { response ->
                val available = response.optBoolean("available", false)
                if (available) {
                    // Username is available, proceed with registration
                    createAccount(mail, pass, uname, fname, lname, date)
                } else {
                    toast("Username '$uname' is already taken")
                }
            },
            onError = { error ->
                android.util.Log.e("Registration", "Username check failed: $error")
                android.util.Log.e("Registration", "API URL: $checkUrl")
                toast("Error: $error\nCheck if PHP server is running at ${ApiConfig.BASE_IP}")
            }
        )
    }

    private fun is18OrOlder(): Boolean {
        if (selectedDob == null) return false
        val eighteenAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
        return !selectedDob!!.after(eighteenAgo)
    }

    private fun createAccount(mail: String, pass: String, uname: String, fname: String, lname: String, date: String) {
        createAccountBtn.isEnabled = false
        createAccountBtn.text = "Creating account..."
        
        val encodedPic = encodeImage(profilePicUri!!)
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        apiClient.post(
            ApiConfig.REGISTER,
            mapOf(
                "email" to mail,
                "password" to pass,
                "username" to uname,
                "firstName" to fname,
                "lastName" to lname,
                "dob" to date,
                "profilePicBase64" to encodedPic,
                "deviceId" to deviceId
            ),
            onSuccess = { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val userId = response.getInt("userId")
                        
                        // Save session locally
                        getSharedPreferences("user_session", MODE_PRIVATE).edit()
                            .putInt("userId", userId)
                            .putString("lastEmail", mail)
                            .putString("lastUsername", uname)
                            .putString("firstName", fname)
                            .putString("lastName", lname)
                            .putString("profilePic", encodedPic)
                            .putBoolean("isLoggedIn", true)
                            .putBoolean("hasEverLoggedIn", true)
                            .apply()

                        toast("Account created successfully")
                        
                        val intent = Intent(this, page5::class.java)
                        intent.putExtra("userId", userId)
                        intent.putExtra("email", mail)
                        intent.putExtra("username", uname)
                        startActivity(intent)
                        finish()
                    } else {
                        val message = response.optString("message", "Registration failed")
                        toast(message)
                        createAccountBtn.isEnabled = true
                        createAccountBtn.text = "Create Account"
                    }
                } catch (e: Exception) {
                    toast("Error: ${e.message}")
                    createAccountBtn.isEnabled = true
                    createAccountBtn.text = "Create Account"
                }
            },
            onError = { error ->
                toast("Sign-up failed: $error")
                createAccountBtn.isEnabled = true
                createAccountBtn.text = "Create Account"
            }
        )
    }

    private fun encodeImage(uri: Uri): String {
        val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        else
            @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
