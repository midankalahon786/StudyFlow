package com.example.lmsapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lmsapp.ui.data.DataClasses.SubmissionSummary
import com.example.lmsapp.ui.network.LMSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentAllReportsViewModel(private val repository: LMSRepository) : ViewModel() {

    private val _submissionSummaries = MutableStateFlow<List<SubmissionSummary>>(emptyList())
    val submissionSummaries: StateFlow<List<SubmissionSummary>> = _submissionSummaries

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchAllStudentReports(token: String, studentId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val result = repository.getAllStudentReports(token, studentId)
                result.onSuccess { reports ->
                    _submissionSummaries.value = reports
                    Log.d("StudentAllReportsViewModel", "Successfully fetched ${reports.size} reports for student: $studentId")
                }.onFailure { e ->
                    _errorMessage.value = e.message ?: "Failed to load all student reports."
                    Log.e("StudentAllReportsViewModel", "Error fetching all student reports: ${e.message}", e)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred."
                Log.e("StudentAllReportsViewModel", "Unexpected error fetching all student reports: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    class StudentAllReportsViewModelFactory(private val repository: LMSRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StudentAllReportsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StudentAllReportsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}