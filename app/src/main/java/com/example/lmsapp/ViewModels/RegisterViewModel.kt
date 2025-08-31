package com.example.lmsapp.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // Import ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras // Import CreationExtras
import com.example.lmsapp.ui.data.DataClasses.StudentRegisterRequest
import com.example.lmsapp.ui.data.DataClasses.TeacherRegisterRequest
import com.example.lmsapp.ui.network.LMSRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: LMSRepository) : ViewModel() {
    var registerStatus by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun registerStudent(request: StudentRegisterRequest) {
        isLoading = true
        registerStatus = "" // Clear previous status
        viewModelScope.launch {
            try {
                val result = repository.registerStudent(request)
                if (result.isSuccess) {
                    registerStatus = "Student registration successful"
                } else {
                    registerStatus = "Student registration failed: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                registerStatus = "Student registration failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun registerTeacher(request: TeacherRegisterRequest) {
        isLoading = true
        registerStatus = "" // Clear previous status
        viewModelScope.launch {
            try {
                val result = repository.registerTeacher(request)
                if (result.isSuccess) {
                    registerStatus = "Teacher registration successful"
                } else {
                    registerStatus = "Teacher registration failed: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                registerStatus = "Teacher registration failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetForm() {
        registerStatus = ""
        isLoading = false
        // Reset other form fields if you add them later
    }

    class RegisterViewModelFactory(private val repository: LMSRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}