package com.example.lmsapp.ViewModels

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Modify the constructor to accept Context for SharedPreferences access
class SharedAuthViewModel(private val context: Context) : ViewModel() {

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()

    private val _role = MutableStateFlow("")
    val role: StateFlow<String> = _role.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()

    // --- Initialization: Load data from SharedPreferences when ViewModel is created ---
    init {
        loadAuthDataFromSharedPreferences()
    }

    fun setAuthData(token: String, role: String, username: String, userId: Int?) {
        _token.value = token
        _role.value = role
        _username.value = username
        _userId.value = userId
        Log.d("SharedAuthViewModel", "setAuthData called with userId: $userId. Saving to prefs.")
        saveAuthDataToSharedPreferences(token, role, username, userId) // Save to persistent storage
    }

    fun clearAuthData() {
        _token.value = ""
        _role.value = ""
        _username.value = ""
        _userId.value = null
        Log.d("SharedAuthViewModel", "clearAuthData called. Clearing from prefs.")
        clearAuthDataFromSharedPreferences() // Clear from persistent storage
    }


    /**
     * Saves authentication data to Android's SharedPreferences.
     * This ensures the data persists across app sessions.
     */
    private fun saveAuthDataToSharedPreferences(token: String, role: String, username: String, userId: Int?) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit() {
            putString("jwt_token", token)
            putString("user_role", role)
            putString("username", username)
            userId?.let { putInt("user_id", it) }
                ?: remove("user_id") // Save userId, remove if null
            // Apply changes asynchronously
        }
        Log.d("SharedAuthViewModel", "Auth data successfully saved to SharedPreferences.")
    }

    /**
     * Loads authentication data from Android's SharedPreferences into the ViewModel's StateFlows.
     * Called during ViewModel initialization.
     */
    private fun loadAuthDataFromSharedPreferences() {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        _token.value = sharedPreferences.getString("jwt_token", "") ?: ""
        _role.value = sharedPreferences.getString("user_role", "") ?: ""
        _username.value = sharedPreferences.getString("username", "") ?: ""
        _userId.value = sharedPreferences.getInt("user_id", -1).takeIf { it != -1 } // -1 as default, then check
        Log.d("SharedAuthViewModel", "Auth data loaded from SharedPreferences. Token length: ${_token.value.length}, Role: ${_role.value}, Username: ${_username.value}, UserId: ${_userId.value}")
    }

    /**
     * Clears all authentication-related data from SharedPreferences.
     */
    private fun clearAuthDataFromSharedPreferences() {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit() {
            remove("jwt_token")
            remove("user_role")
            remove("username")
            remove("user_id")
        }
    }

    // --- ViewModel Factory (Necessary for Context injection) ---

    // Factory for SharedAuthViewModel, now requiring Context
    class SharedAuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(SharedAuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SharedAuthViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}