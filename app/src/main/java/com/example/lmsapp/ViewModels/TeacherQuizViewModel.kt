package com.example.lmsapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lmsapp.ui.data.Course
import com.example.lmsapp.ui.data.CreateQuizRequest
import com.example.lmsapp.ui.data.Question
import com.example.lmsapp.ui.data.DataClasses.Quiz
import com.example.lmsapp.ui.network.LMSRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update // Make sure this import is present

class TeacherQuizViewModel(
    private val repository: LMSRepository,
    private val sharedAuthViewModel: SharedAuthViewModel // Inject SharedAuthViewModel
) : ViewModel() {

    // Quiz input states (for creating/editing a single quiz)
    var quizTitle = MutableStateFlow("")
    var timeLimit = MutableStateFlow(0)
    var negativeMarking = MutableStateFlow(0.0f)
    var totalMarks = MutableStateFlow(0)

    private val _availableCourses = MutableStateFlow<List<Course>>(emptyList())
    val availableCourses: StateFlow<List<Course>> = _availableCourses.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    // State for overall loading (e.g., fetching quiz list)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State for the list of quizzes
    private val _quizList = MutableStateFlow<List<Quiz>>(emptyList())
    val quizList: StateFlow<List<Quiz>> = _quizList.asStateFlow()

    // State for quiz creation success
    private val _quizCreationSuccess = MutableStateFlow<Boolean?>(null)
    val quizCreationSuccess: StateFlow<Boolean?> = _quizCreationSuccess.asStateFlow()

    // New: Enum for deletion status
    enum class DeletionStatus { IDLE, IN_PROGRESS, SUCCESS, FAILED }

    private val _deletionStatus = MutableStateFlow(DeletionStatus.IDLE)
    val deletionStatus: StateFlow<DeletionStatus> = _deletionStatus.asStateFlow()

    // New: State for quiz update success (optional, similar to creation)
    private val _quizUpdateSuccess = MutableStateFlow<Boolean?>(null)
    val quizUpdateSuccess: StateFlow<Boolean?> = _quizUpdateSuccess.asStateFlow()




    fun addQuestion(questionText: String, options: List<String>, correctAnswer: String, marks: Int) {
        val correctAnswerIndex = when (correctAnswer.uppercase()) {
            "A" -> 0
            "B" -> 1
            "C" -> 2
            "D" -> 3
            else -> 0
        }
        val question = Question(
            // Frontend UI ID: This ID is for managing questions in the UI before sending to backend.
            // The backend will assign its own persistent unique IDs.
            id = (_questions.value.maxOfOrNull { it.id } ?: 0) + 1,
            questionText = questionText,
            options = options,
            correctOptionIndex = correctAnswerIndex,
            mark = marks
        )
        _questions.value = _questions.value + question
    }

    fun createQuiz(apiToken: String) {
        viewModelScope.launch {
            val teacherId = sharedAuthViewModel.userId.firstOrNull()

            if (teacherId == null) {
                Log.e("TeacherQuizViewModel", "Cannot create quiz: Teacher ID is null.")
                _quizCreationSuccess.value = false
                return@launch
            }

            if (apiToken.isBlank()){
                Log.e("TeacherQuizViewModel", "Cannot create quiz: API Token is blank.")
                _quizCreationSuccess.value = false
                return@launch
            }

            val newQuizRequest = CreateQuizRequest(
                title = quizTitle.value,
                timeLimit = timeLimit.value,
                negativeMarking = negativeMarking.value,
                totalMarks = totalMarks.value,
                questions = _questions.value,
                createdBy = teacherId

            )

            val gson = Gson()
            Log.d("TeacherQuizViewModel", "Creating quiz with request: ${gson.toJson(newQuizRequest)}")

            _isLoading.value = true
            val result = repository.createQuiz(apiToken, newQuizRequest)
            _isLoading.value = false
            result.onSuccess {
                Log.d("TeacherQuizViewModel", "Quiz created successfully.")
                _quizCreationSuccess.value = true
                _questions.value = emptyList() // Clear questions on success
                quizTitle.value = ""
                timeLimit.value = 0
                negativeMarking.value = 0.0f
                totalMarks.value = 0
                // Optionally refetch quizzes to update the list immediately
                fetchQuizzes(apiToken)
            }.onFailure { error ->
                Log.e("TeacherQuizViewModel", "Failed to create quiz: ${error.message}")
                _quizCreationSuccess.value = false
            }
        }
    }



    fun resetQuizCreationSuccess() {
        _quizCreationSuccess.value = null
    }

    fun fetchQuizzes(apiToken: String) {
        viewModelScope.launch {
            if (apiToken.isBlank()) {
                Log.e("TeacherQuizViewModel", "Cannot fetch quizzes: API Token is blank.")
                return@launch
            }
            try {
                _isLoading.value = true
                val result = repository.getAllQuizzes(apiToken)
                _isLoading.value = false
                result.onSuccess { quizzes ->
                    _quizList.value = quizzes
                    Log.d("TeacherQuizViewModel", "Fetched quizzes successfully.")
                }.onFailure { error ->
                    Log.e("TeacherQuizViewModel", "Failed to fetch quizzes: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("TeacherQuizViewModel", "Error fetching quizzes: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    // --- NEW: Function to delete a quiz ---
    fun deleteQuiz(token: String, quizId: Int) {
        _deletionStatus.value = DeletionStatus.IN_PROGRESS
        viewModelScope.launch {
            try {
                val result = repository.deleteQuiz(token, quizId)
                result.onSuccess {
                    Log.d("TeacherQuizViewModel", "Quiz $quizId deleted successfully.")
                    _deletionStatus.value = DeletionStatus.SUCCESS
                    // Optimistically update the UI by removing the deleted quiz
                    _quizList.update { currentList -> currentList.filter { it.id != quizId } }
                    // Or, you could refetch the entire list: fetchQuizzes(token)
                }.onFailure { e ->
                    Log.e("TeacherQuizViewModel", "Failed to delete quiz $quizId: ${e.message}")
                    _deletionStatus.value = DeletionStatus.FAILED
                }
            } catch (e: Exception) {
                Log.e("TeacherQuizViewModel", "An unexpected error occurred during deleteQuiz: ${e.message}", e)
                _deletionStatus.value = DeletionStatus.FAILED
            }
        }
    }

    // --- NEW: Function to reset deletion status ---
    fun resetDeletionStatus() {
        _deletionStatus.value = DeletionStatus.IDLE
    }

    // --- NEW: Function to update a quiz (for editing) ---
    fun updateQuiz(token: String, quizId: Int, updatedQuiz: Quiz) {
        _isLoading.value = true // Or a separate update loading state
        _quizUpdateSuccess.value = null // Reset update status
        viewModelScope.launch {
            try {
                val result = repository.updateQuiz(token, quizId, updatedQuiz)
                _isLoading.value = false
                result.onSuccess { returnedQuiz ->
                    Log.d("TeacherQuizViewModel", "Quiz $quizId updated successfully.")
                    _quizUpdateSuccess.value = true
                    // Update the quiz in the local list
                    _quizList.update { currentList ->
                        currentList.map { if (it.id == returnedQuiz.id) returnedQuiz else it }
                    }
                }.onFailure { e ->
                    Log.e("TeacherQuizViewModel", "Failed to update quiz $quizId: ${e.message}")
                    _quizUpdateSuccess.value = false
                }
            } catch (e: Exception) {
                Log.e("TeacherQuizViewModel", "An unexpected error occurred during updateQuiz: ${e.message}", e)
                _quizUpdateSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- NEW: Function to reset update status ---
    fun resetQuizUpdateSuccess() {
        _quizUpdateSuccess.value = null
    }

    fun loadQuizForEdit(token: String, quizId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getQuizById(token, quizId)
                _isLoading.value = false
                result.onSuccess { quiz ->
                    quizTitle.value = quiz.title
                    timeLimit.value = quiz.timeLimit
                    negativeMarking.value = quiz.negativeMarking
                    totalMarks.value = quiz.totalMarks
                    _questions.value = quiz.questions // Load existing questions
                    Log.d("TeacherQuizViewModel", "Loaded quiz $quizId for editing.")
                }.onFailure { e ->
                    Log.e("TeacherQuizViewModel", "Failed to load quiz $quizId for editing: ${e.message}")
                    // Handle error, e.g., navigate back or show a message
                }
            } catch (e: Exception) {
                Log.e("TeacherQuizViewModel", "Unexpected error loading quiz for editing: ${e.message}", e)
            }
        }
    }

    fun updateQuestionInList(updatedQuestion: Question) {
        _questions.update { currentQuestions ->
            currentQuestions.map {
                if (it.id == updatedQuestion.id) { // Use ID to identify the question
                    updatedQuestion
                } else {
                    it
                }
            }
        }
    }

    fun removeQuestionFromList(questionToRemove: Question) {
        _questions.update { currentQuestions ->
            currentQuestions.filter { questionInList ->
                if (true) {
                    questionInList.id != questionToRemove.id
                } else {
                    questionInList !== questionToRemove
                }
            }
        }
    }
    // --- ViewModel Factory for TeacherQuizViewModel ---
    class TeacherQuizViewModelFactory(
        private val repository: LMSRepository,
        private val sharedAuthViewModel: SharedAuthViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TeacherQuizViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TeacherQuizViewModel(repository, sharedAuthViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}