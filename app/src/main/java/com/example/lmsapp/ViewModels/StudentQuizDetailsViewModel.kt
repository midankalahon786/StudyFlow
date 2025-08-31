package com.example.lmsapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.lmsapp.ui.data.DataClasses.Quiz
import com.example.lmsapp.ui.network.LMSRepository
import kotlinx.coroutines.launch

class StudentQuizDetailsViewModel(private val repository: LMSRepository) : ViewModel() {

    private val _quizDetails = MutableStateFlow<Quiz?>(null)
    val quizDetails: StateFlow<Quiz?> = _quizDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchQuizDetails(token: String, quizId: Int?) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getQuizById(token, quizId)
            result.onSuccess { quiz ->
                _quizDetails.value = quiz
            }.onFailure {
                // Handle error state (e.g. show a toast or log)
            }
            _isLoading.value = false
        }
    }



    class StudentQuizDetailsViewModelFactory(private val repository: LMSRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(StudentQuizDetailsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StudentQuizDetailsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
