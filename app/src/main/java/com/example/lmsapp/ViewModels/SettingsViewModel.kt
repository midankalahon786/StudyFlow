package com.example.lmsapp.ViewModels

import android.util.Log // Import Log for debugging
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.lmsapp.ui.network.LMSRepository // Import your LMSRepository
import kotlinx.coroutines.launch

// Modify the constructor to take LMSRepository instead of ApiService
class SettingsViewModel(private val lmsRepository: LMSRepository) : ViewModel() {

    var changePasswordStatus by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun changePassword(token: String, oldPass: String, newPass: String) {
        isLoading = true
        changePasswordStatus = ""

        viewModelScope.launch {
            try {

                val result = lmsRepository.changePassword(token, oldPass, newPass)
                result.onSuccess { response ->
                    val successMessage = response.message
                    changePasswordStatus = successMessage
                    Log.d("SettingsViewModel", "Password change successful: $successMessage")
                }.onFailure { exception ->
                    // Handle failure response from LMSRepository's Result
                    val errorMessage = exception.message ?: "An unknown error occurred."
                    changePasswordStatus = "Error: $errorMessage"
                    Log.e("SettingsViewModel", "Password change failed: $errorMessage", exception)
                }

            } catch (e: Exception) {
                val errorMessage = "An unexpected error occurred: ${e.message ?: "No message"}"
                changePasswordStatus = "Error: $errorMessage"
                Log.e("SettingsViewModel", "General Exception during password change: $errorMessage", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun resetStatus() {
        changePasswordStatus = ""
    }

    // Modify the factory to provide LMSRepository
    class SettingsViewModelFactory(
        private val lmsRepository: LMSRepository // Change from ApiService to LMSRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(lmsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}