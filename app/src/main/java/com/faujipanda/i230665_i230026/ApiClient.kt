package com.faujipanda.i230665_i230026

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONObject

/**
 * API Client for making HTTP requests using Volley
 * Handles GET and POST requests with JSON responses
 */
class ApiClient(private val context: Context) {
    
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }
    
    private val gson = Gson()
    
    companion object {
        private const val TAG = "ApiClient"
    }
    
    /**
     * Check if device is connected to internet
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Make a POST request with JSON body
     */
    fun post(
        url: String,
        params: Map<String, Any>,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "POST Request to: $url")
        Log.d(TAG, "POST Params: $params")
        
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No network connection available")
            onError("No internet connection. Please check your WiFi/Data.")
            return
        }
        
        try {
            val jsonBody = JSONObject(params)
            Log.d(TAG, "POST Body: $jsonBody")
            
            val request = JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                { response ->
                    Log.d(TAG, "POST Success: $response")
                    onSuccess(response)
                },
                { error ->
                    val errorMessage = when {
                        error.networkResponse == null -> {
                            Log.e(TAG, "Network error - no response from server")
                            Log.e(TAG, "URL: $url")
                            Log.e(TAG, "Error: ${error.message}")
                            "Cannot connect to server at ${ApiConfig.BASE_IP}.\n\nPlease ensure:\n1. XAMPP is running\n2. Apache is started\n3. Your PC IP is ${ApiConfig.BASE_IP}\n4. Phone and PC are on same WiFi"
                        }
                        error.networkResponse.statusCode == 404 -> {
                            Log.e(TAG, "404 - API endpoint not found: $url")
                            "API endpoint not found. Please check PHP files are in correct location."
                        }
                        error.networkResponse.statusCode == 500 -> {
                            Log.e(TAG, "500 - Server error")
                            val errorData = String(error.networkResponse.data)
                            Log.e(TAG, "Server error response: $errorData")
                            "Server error. Check XAMPP error logs."
                        }
                        error.networkResponse.data != null -> {
                            try {
                                val errorData = String(error.networkResponse.data)
                                Log.e(TAG, "Error response: $errorData")
                                val errorJson = JSONObject(errorData)
                                errorJson.optString("message", "Unknown error occurred")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing error response", e)
                                "Error: ${error.message}"
                            }
                        }
                        else -> {
                            Log.e(TAG, "Unknown error: ${error.message}")
                            "Error: ${error.message}"
                        }
                    }
                    onError(errorMessage)
                }
            )
            
            // Set timeout to 30 seconds
            request.setRetryPolicy(
                com.android.volley.DefaultRetryPolicy(
                    30000,
                    0,
                    com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
            
            requestQueue.add(request)
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating request", e)
            onError("Error creating request: ${e.message}")
        }
    }
    
    /**
     * Make a GET request
     */
    fun get(
        url: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "GET Request to: $url")
        
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No network connection available")
            onError("No internet connection. Please check your WiFi/Data.")
            return
        }
        
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                Log.d(TAG, "GET Success: $response")
                onSuccess(response)
            },
            { error ->
                val errorMessage = when {
                    error.networkResponse == null -> {
                        Log.e(TAG, "Network error - no response from server")
                        Log.e(TAG, "URL: $url")
                        "Cannot connect to server at ${ApiConfig.BASE_IP}.\n\nPlease ensure:\n1. XAMPP is running\n2. Apache is started\n3. Your PC IP is ${ApiConfig.BASE_IP}\n4. Phone and PC are on same WiFi"
                    }
                    error.networkResponse.statusCode == 404 -> {
                        Log.e(TAG, "404 - API endpoint not found: $url")
                        "API endpoint not found"
                    }
                    error.networkResponse.statusCode == 500 -> {
                        Log.e(TAG, "500 - Server error")
                        "Server error. Check XAMPP logs."
                    }
                    error.networkResponse.data != null -> {
                        try {
                            val errorData = String(error.networkResponse.data)
                            Log.e(TAG, "Error response: $errorData")
                            val errorJson = JSONObject(errorData)
                            errorJson.optString("message", "Unknown error occurred")
                        } catch (e: Exception) {
                            "Error: ${error.message}"
                        }
                    }
                    else -> "Error: ${error.message}"
                }
                onError(errorMessage)
            }
        )
        
        // Set timeout to 30 seconds
        request.setRetryPolicy(
            com.android.volley.DefaultRetryPolicy(
                30000,
                0,
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        
        requestQueue.add(request)
    }
    
    /**
     * Build URL with query parameters
     */
    fun buildUrlWithParams(baseUrl: String, params: Map<String, String>): String {
        if (params.isEmpty()) return baseUrl
        
        val queryString = params.entries.joinToString("&") { (key, value) ->
            "$key=${java.net.URLEncoder.encode(value, "UTF-8")}"
        }
        
        return "$baseUrl?$queryString"
    }
}
