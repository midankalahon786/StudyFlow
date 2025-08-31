package com.example.lmsapp.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // Import ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras // Import CreationExtras
import com.example.lmsapp.ui.network.LMSRepository
import kotlinx.coroutines.launch
import android.util.Log

class LoginViewModel(
    private val repository: LMSRepository,
    private val sharedAuthViewModel: SharedAuthViewModel
) : ViewModel() {

    var token by mutableStateOf<String?>(null)
        internal set

    var userRole by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(username: String, password: String) {
        isLoading = true
        errorMessage = null // Clear any previous error message

        viewModelScope.launch {
            Log.d("LoginViewModel", "Login attempt with username: $username")
            try {
                val result = repository.login(username, password)

                if (result.isSuccess) {
                    val loginResponse = result.getOrThrow()
                    token = loginResponse.token
                    userRole = loginResponse.user.role
                    val userId = loginResponse.user.id // Access userId from loginResponse.user.id

                    // Update SharedAuthViewModel with all the data for persistence
                    // Ensure token and userRole are non-null before passing, as per SharedAuthViewModel's contract
                    if (token != null && userRole != null) {
                        sharedAuthViewModel.setAuthData(token!!, userRole!!, username, userId)
                        Log.d("LoginViewModel", "Login successful. Token and Role updated. UserId: $userId")
                    } else {
                        // This case should ideally not happen if backend guarantees token/role on success
                        errorMessage = "Login successful but token or role is missing."
                        Log.e("LoginViewModel", "Login successful but token ($token) or role ($userRole) is null.")
                    }
                } else {
                    // Handle login failure from repository
                    val exception = result.exceptionOrNull()
                    errorMessage = exception?.message ?: "Invalid username or password."
                    Log.e("LoginViewModel", "Login failed: ${exception?.message}", exception)
                }
            } catch (e: Exception) {
                // Handle unexpected exceptions during the login process (e.g., network issues)
                errorMessage = "Login failed: ${e.message ?: "An unknown error occurred."}"
                Log.e("LoginViewModel", "Login failed with unexpected error: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        token = null
        userRole = null
        sharedAuthViewModel.clearAuthData() // Clear persistent data via SharedAuthViewModel
        Log.d("LoginViewModel", "User logged out. Auth data cleared.")
    }

    // --- ViewModel Factory for LoginViewModel ---
    class LoginViewModelFactory(
        private val repository: LMSRepository,
        private val sharedAuthViewModel: SharedAuthViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(repository, sharedAuthViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
