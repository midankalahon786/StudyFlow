package com.example.lmsapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lmsapp.ui.data.DataClasses.Report // You will create this Data Class next
import com.example.lmsapp.ui.network.LMSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel(private val repository: LMSRepository) : ViewModel() {

    private val _quizReport = MutableStateFlow<Report?>(null)
    val quizReport: StateFlow<Report?> = _quizReport

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchQuizReport(token: String, quizId: Int, studentId: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                // Assuming LMSRepository has a getQuizReport method
                val result = repository.getQuizReport(token, quizId, studentId)
                result.onSuccess { report ->
                    _quizReport.value = report
                    Log.d("ReportViewModel", "Successfully fetched quiz report for quizId: $quizId, studentId: $studentId")
                    Log.d("ReportViewModelData", "Parsed Report Object: $report")

                }.onFailure { e ->
                    _errorMessage.value = e.message ?: "Failed to load quiz report."
                    Log.e("ReportViewModel", "Error fetching quiz report: ${e.message}", e)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred."
                Log.e("ReportViewModel", "An unexpected error occurred: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }

    }
    fun clearReport() {
        _quizReport.value = null
        _errorMessage.value = null // Also clear any previous error messages
    }

    // --- ViewModel Factory for ReportViewModel ---
    class ReportViewModelFactory(private val repository: LMSRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ReportViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}