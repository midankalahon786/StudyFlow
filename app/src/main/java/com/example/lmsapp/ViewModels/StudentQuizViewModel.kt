package com.example.lmsapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lmsapp.ui.data.Question
import com.example.lmsapp.ui.network.LMSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class StudentQuizViewModel(
    private val repository: LMSRepository,
    private val sharedAuthViewModel: SharedAuthViewModel
) : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions

    private val _studentAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val studentAnswers: StateFlow<Map<Int, String>> = _studentAnswers

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _totalMarks = MutableStateFlow(0)
    val totalMarks: StateFlow<Int> = _totalMarks

    private val _timeLimit = MutableStateFlow(0)
    val timeLimit: StateFlow<Int> = _timeLimit

    enum class SubmissionStatus { IDLE, IN_PROGRESS, SUCCESS, FAILED }

    private val _submissionStatus = MutableStateFlow(SubmissionStatus.IDLE)
    val submissionStatus: StateFlow<SubmissionStatus> = _submissionStatus

    fun fetchQuizById(token: String, quizId: Int) {
        viewModelScope.launch {
            val result = repository.getQuizById(token, quizId)
            result.onSuccess { quiz ->
                _questions.value = quiz.questions
                _totalMarks.value = quiz.totalMarks
                _timeLimit.value = quiz.timeLimit
            }.onFailure { error ->
                Log.e("StudentQuizViewModel", "Error fetching quiz: ${error.message}")
            }
        }
    }

    fun setAnswer(questionId: Int, selectedAnswer: String) {
        Log.d("StudentQuizViewModel", "Setting answer for QID: $questionId, Option: $selectedAnswer")
        _studentAnswers.update { currentMap ->
            // This creates a new map by adding/updating the entry
            currentMap + (questionId to selectedAnswer)
        }
        Log.d("StudentQuizViewModel", "Current studentAnswers: ${_studentAnswers.value}")
    }

    fun submitQuiz(token: String, quizId: Int) {
        viewModelScope.launch {
            var calculatedScore = 0
            _questions.value.forEach { question ->
                val studentSelectedOption = _studentAnswers.value[question.id]

                if (studentSelectedOption != null && question.correctOptionIndex >= 0 && question.correctOptionIndex < question.options.size) {
                    if (studentSelectedOption == question.options[question.correctOptionIndex]) {
                        calculatedScore += question.mark
                    }
                }
            }
            _score.value = calculatedScore

            // Get the studentId from SharedAuthViewModel
            val studentId = sharedAuthViewModel.userId.value // Assuming userId is studentId for student role
            if (studentId == null) {
                Log.e("StudentQuizViewModel", "Error: Student ID is null, cannot submit quiz.")
                // You might want to show an error message to the user here
                return@launch // Stop execution if studentId is null
            }

            val result = repository.submitQuiz(token, quizId, _studentAnswers.value, calculatedScore, studentId)
            result.onSuccess {
                Log.d("StudentQuizViewModel", "Quiz submission successful to backend for Quiz ID: $quizId, Score: $calculatedScore")
            }.onFailure { error ->
                Log.e("StudentQuizViewModel", "Quiz submission failed to backend: ${error.message}")
            }
        }
    }

    fun resetSubmissionStatus() {
        _submissionStatus.value = SubmissionStatus.IDLE
    }

    class StudentQuizViewModelFactory(
        private val repository: LMSRepository,
        private val sharedAuthViewModel: SharedAuthViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StudentQuizViewModel::class.java)) {
                return StudentQuizViewModel(repository, sharedAuthViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}