package com.example.lmsapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.lmsapp.ui.data.DataClasses.Quiz
import com.example.lmsapp.ui.network.LMSRepository
import kotlinx.coroutines.launch

class StudentQuizListViewModel(private val repository: LMSRepository) : ViewModel() {

    private val _quizList = MutableStateFlow<List<Quiz>>(emptyList())
    val quizList: StateFlow<List<Quiz>> = _quizList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchQuizzes(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch the quizzes from the repository
                val result = repository.getAllQuizzes(token)

                // Handle success and failure
                result.onSuccess { quizzes ->
                    // If successful, update the quiz list
                    _quizList.value = quizzes
                }.onFailure { error ->
                    // If an error occurs, handle it
                    // You can log the error or show a message to the user
                    Log.e("QuizFetchError", "Error fetching quizzes", error)
                }
            } catch (e: Exception) {
                // Log any other exceptions if necessary
                Log.e("QuizFetchError", "Unexpected error", e)
            } finally {
                // Ensure that loading state is reset after the operation
                _isLoading.value = false
            }
        }
    }



    // Public method to set quiz list for preview
    fun setQuizzes(quizzes: List<Quiz>) {
        _quizList.value = quizzes
    }

    // Public method to set loading state for preview
    fun setIsLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
    class StudentQuizListViewModelFactory(private val repository: LMSRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(StudentQuizListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StudentQuizListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


}
