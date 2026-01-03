package com.faujipanda.i230665_i230026

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnAttach: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var imgProfile: CircleImageView
    private lateinit var txtUsername: TextView
    private lateinit var txtStatus: TextView
    private lateinit var btnVoiceCall: ImageView
    private lateinit var btnVideoCall: ImageView

    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Map<String, Any>>()
    private lateinit var apiClient: ApiClient

    private var userId: Int = 0
    private var otherUserId: Int = 0
    private var otherUsername: String = ""
    private var otherProfilePic: String = ""
    private var lastMessageId: Int = 0

    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2
    
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val shared = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = intent.getIntExtra("userId", 0)
        if (userId == 0) {
            userId = shared.getInt("userId", 0)
        }
        
        otherUserId = intent.getIntExtra("otherUserId", 0)
        otherUsername = intent.getStringExtra("otherUsername") ?: ""
        otherProfilePic = intent.getStringExtra("otherProfilePic") ?: ""

        if (userId == 0) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        apiClient = ApiClient(this)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadMessages()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        btnBack = findViewById(R.id.btnBack)
        imgProfile = findViewById(R.id.imgProfile)
        txtUsername = findViewById(R.id.txtUsername)
        txtStatus = findViewById(R.id.txtStatus)
        btnVoiceCall = findViewById(R.id.btnVoiceCall)
        btnVideoCall = findViewById(R.id.btnVideoCall)

        txtUsername.text = otherUsername
        if (otherProfilePic.isNotEmpty()) {
            try {
                val cleanBase64 = otherProfilePic.replace("data:image/jpeg;base64,", "")
                    .replace("data:image/png;base64,", "")
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgProfile.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imgProfile.setImageResource(R.drawable.pfp_image)
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        
        adapter = MessageAdapter(this, messages, userId) { message, action ->
            val timestamp = message["timestamp"] as Long
            val currentTime = System.currentTimeMillis()
            val fiveMinutesInMillis = 5 * 60 * 1000
            
            if (currentTime - timestamp > fiveMinutesInMillis) {
                Toast.makeText(this, "You can only $action messages within 5 minutes of sending", Toast.LENGTH_SHORT).show()
                return@MessageAdapter
            }

            when (action) {
                "edit" -> showEditDialog(message)
                "delete" -> showDeleteDialog(message)
            }
        }
        recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text, "", "")
                etMessage.text.clear()
            }
        }

        btnAttach.setOnClickListener {
            showAttachmentOptions()
        }
        
        btnVoiceCall.setOnClickListener { initiateCall("voice") }
        btnVideoCall.setOnClickListener { initiateCall("video") }
    }

    private fun initiateCall(type: String) {
        val channelName = "call_${userId}_${otherUserId}_${System.currentTimeMillis()}"
        
        Toast.makeText(this, "Initiating $type call...", Toast.LENGTH_SHORT).show()
        
        val params = mapOf(
            "callerId" to userId,
            "receiverId" to otherUserId,
            "callType" to type,
            "channelName" to channelName
        )
        
        apiClient.post(ApiConfig.INITIATE_CALL, params, onSuccess = { response ->
            val success = response.optBoolean("success", false)
            if (success) {
                val intent = if (type == "voice") {
                    Intent(this, VoiceCallActivity::class.java)
                } else {
                    Intent(this, VideoCallActivity::class.java)
                }
                intent.putExtra("channelName", channelName)
                intent.putExtra("otherUsername", otherUsername)
                intent.putExtra("otherProfilePic", otherProfilePic)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Failed to initiate call", Toast.LENGTH_SHORT).show()
            }
        }, onError = { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
        })
    }

    private fun showAttachmentOptions() {
        val options = arrayOf("Gallery", "Camera")
        AlertDialog.Builder(this)
            .setTitle("Select Media")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    data?.data?.let { uri ->
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        val base64 = bitmapToBase64(bitmap)
                        sendMessage("", base64, "image")
                    }
                }
                CAPTURE_IMAGE_REQUEST -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        val base64 = bitmapToBase64(it)
                        sendMessage("", base64, "image")
                    }
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun sendMessage(text: String, mediaBase64: String, mediaType: String) {
        val params = mapOf(
            "senderId" to userId,
            "receiverId" to otherUserId,
            "text" to text,
            "mediaBase64" to mediaBase64,
            "mediaType" to mediaType
        )
        
        apiClient.post(ApiConfig.SEND_MESSAGE, params, onSuccess = { response ->
            val success = response.optBoolean("success", false)
            if (success) {
                loadMessages() // Reload to show sent message
            } else {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }, onError = { error ->
            Toast.makeText(this, "Error sending message: $error", Toast.LENGTH_SHORT).show()
        })
    }

    private fun loadMessages() {
        val url = apiClient.buildUrlWithParams(ApiConfig.GET_MESSAGES, mapOf(
            "userId" to userId.toString(),
            "otherUserId" to otherUserId.toString(),
            "lastId" to lastMessageId.toString()
        ))
        
        apiClient.get(url, onSuccess = { response ->
            val success = response.optBoolean("success", false)
            if (success) {
                val messagesArray = response.optJSONArray("messages")
                
                if (messagesArray != null && messagesArray.length() > 0) {
                    if (lastMessageId == 0) {
                        messages.clear()
                    }
                    
                    for (i in 0 until messagesArray.length()) {
                        val msg = messagesArray.getJSONObject(i)
                        val messageMap = mutableMapOf<String, Any>()
                        messageMap["id"] = msg.optInt("id")
                        messageMap["serverMessageId"] = msg.optString("id", "")
                        messageMap["senderId"] = msg.optInt("senderId")
                        messageMap["receiverId"] = msg.optInt("receiverId")
                        messageMap["text"] = msg.optString("text", "")
                        messageMap["mediaBase64"] = msg.optString("mediaBase64", "")
                        messageMap["mediaType"] = msg.optString("mediaType", "")
                        messageMap["timestamp"] = msg.optLong("timestamp")
                        messageMap["isEdited"] = msg.optInt("isEdited", 0)
                        
                        messages.add(messageMap)
                        
                        val msgId = msg.optInt("id")
                        if (msgId > lastMessageId) {
                            lastMessageId = msgId
                        }
                    }
                    
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
        }, onError = { error ->
            // Silently fail during polling
        })
    }

    private val messagePolling = object : Runnable {
        override fun run() {
            loadMessages()
            handler.postDelayed(this, 2000) // Poll every 2 seconds
        }
    }
    
    private val statusPolling = object : Runnable {
        override fun run() {
            checkUserStatus()
            handler.postDelayed(this, 3000) // Poll every 3 seconds
        }
    }
    
    private val callPolling = object : Runnable {
        override fun run() {
            checkIncomingCall()
            handler.postDelayed(this, 3000) // Poll every 3 seconds
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(messagePolling)
        handler.post(statusPolling)
        handler.post(callPolling)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(messagePolling)
        handler.removeCallbacks(statusPolling)
        handler.removeCallbacks(callPolling)
    }

    private fun checkUserStatus() {
        val url = "${ApiConfig.GET_USER_STATUS}?userId=$otherUserId"
        apiClient.get(url, onSuccess = { response ->
            val isOnline = response.optBoolean("isOnline", false)
            if (isOnline) {
                txtStatus.text = "Online"
                txtStatus.setTextColor(android.graphics.Color.GREEN)
            } else {
                txtStatus.text = "Offline"
                txtStatus.setTextColor(android.graphics.Color.GRAY)
            }
        }, onError = { error ->
            // Ignore error
        })
    }
    
    private fun checkIncomingCall() {
        val url = "${ApiConfig.CHECK_INCOMING_CALL}?userId=$userId"
        apiClient.get(url, onSuccess = { response ->
            val hasCall = response.optBoolean("hasCall", false)
            if (hasCall) {
                val call = response.optJSONObject("call")
                call?.let {
                    val callId = it.optInt("id")
                    val callerId = it.optInt("callerId")
                    val callerName = it.optString("callerName")
                    val callerPic = it.optString("callerPic")
                    val channelName = it.optString("channelName")
                    val callType = it.optString("type")
                    
                    // Open incoming call activity
                    val intent = Intent(this, CallIncomingActivity::class.java)
                    intent.putExtra("callId", callId)
                    intent.putExtra("channelName", channelName)
                    intent.putExtra("callerName", callerName)
                    intent.putExtra("callerPic", callerPic)
                    intent.putExtra("callType", callType)
                    startActivity(intent)
                }
            }
        }, onError = {
            // Silently ignore
        })
    }

    private fun showEditDialog(message: Map<String, Any>) {
        val editText = EditText(this)
        editText.setText(message["text"] as String)
        
        AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    val params = mapOf(
                        "messageId" to message["serverMessageId"].toString(),
                        "newText" to newText
                    )
                    apiClient.post(ApiConfig.EDIT_MESSAGE, params, onSuccess = {
                        loadMessages()
                    }, onError = {})
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(message: Map<String, Any>) {
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                val params = mapOf("messageId" to message["serverMessageId"].toString())
                apiClient.post(ApiConfig.DELETE_MESSAGE, params, onSuccess = {
                    loadMessages()
                }, onError = {})
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
