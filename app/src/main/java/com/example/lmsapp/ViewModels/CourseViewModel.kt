
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.lmsapp.ui.data.Course
import com.example.lmsapp.ui.data.DataClasses.Resource
import com.example.lmsapp.ui.data.DataClasses.Student
import com.example.lmsapp.ui.data.DataClasses.UIState
import com.example.lmsapp.ui.network.LMSRepository
import com.example.lmsapp.ui.network.UpdateCourseStudentsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody


@Suppress("UNCHECKED_CAST")
open class CourseViewModel(
    private val repository: LMSRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _studentList = MutableStateFlow<UIState<List<Student>>>(UIState.Loading)
    val studentList: StateFlow<UIState<List<Student>>> = _studentList.asStateFlow()

    private val _courses = MutableStateFlow<UIState<List<Course>>>(UIState.Loading)
    val courses: StateFlow<UIState<List<Course>>> = _courses.asStateFlow()

    private val _courseCreationResult = MutableStateFlow<UIState<Course>?>(null)
    val courseCreationResult: StateFlow<UIState<Course>?> = _courseCreationResult.asStateFlow()

    private val _selectedCourse = MutableStateFlow<UIState<Course?>>(UIState.Loading)
    val selectedCourse: StateFlow<UIState<Course?>> = _selectedCourse.asStateFlow()

    private val _isFetchingCourses = MutableStateFlow(false)
    val isFetchingCourses: StateFlow<Boolean> = _isFetchingCourses.asStateFlow()

    private val _isCreatingCourse = MutableStateFlow(false)
    val isCreatingCourse: StateFlow<Boolean> = _isCreatingCourse

    private val _courseResources = MutableStateFlow<UIState<List<Resource>>>(UIState.Loading)
    val courseResources: StateFlow<UIState<List<Resource>>> = _courseResources.asStateFlow()

    private val _token = MutableStateFlow(savedStateHandle["token"] ?: "")
    val token: StateFlow<String> = _token.asStateFlow()

    fun setToken(value: String) {
        _token.value = value
        savedStateHandle["token"] = value
    }

    // Simplified fetchStudents using cache
    @OptIn(ExperimentalCoroutinesApi::class)
    val students: StateFlow<UIState<List<Student>>> = token.flatMapLatest { tokenValue ->
        flow {
            if (tokenValue.isBlank()) {
                emit(UIState.Success(emptyList()))
                return@flow
            }
            Log.d("CourseViewModel", "Fetching all students with token: Bearer $tokenValue")
            val result = withContext(Dispatchers.IO) {
                repository.getStudents("Bearer $tokenValue")
            }
            result.fold(
                onSuccess = { studentList ->
                    Log.d("CourseViewModel", "Successfully fetched all students: $studentList")
                    emit(UIState.Success(studentList))
                },
                onFailure = { error ->
                    Log.e("CourseViewModel", "Error fetching all students: ${error.message}", error)
                    emit(UIState.Error("Error fetching all students: ${error.message}", error))
                }
            )
        }.catch { e ->
            Log.e("CourseViewModel", "Exception fetching all students", e)
            emit(UIState.Error("Exception fetching all students: ${e.localizedMessage}", e))
        }.flowOn(Dispatchers.IO)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UIState.Loading)


    init {
        viewModelScope.launch {
            token.collect { currentToken ->
                if (currentToken.isNotBlank()) {
                    fetchCoursesWithRole(currentToken)
                }
            }
        }
    }

    fun fetchStudents(token: String) {
        viewModelScope.launch {
            try {
                val bearerToken = token
                Log.d("CourseViewModel", "Fetching students with token: $bearerToken")
                val result = withContext(Dispatchers.IO) {
                    repository.getStudents(bearerToken)
                }
                result.fold(
                    onSuccess = { students ->
                        Log.d("CourseViewModel", "Successfully fetched students: $students")
                        _studentList.value = UIState.Success(students)
                    },
                    onFailure = { error ->
                        Log.e("CourseViewModel", "Failed to load students: ${error.message}", error)
                        _studentList.value = UIState.Error("Failed to load students: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Unexpected error: ${e.message}", e)
                _studentList.value = UIState.Error("Unexpected error: ${e.message}", e)
            }
        }
    }

    fun fetchCoursesWithRole(token: String) {
        viewModelScope.launch {
            _isFetchingCourses.value = true
            try {
                val bearerToken = "Bearer $token"
                Log.d("CourseViewModel", "Fetching courses with token: $bearerToken")
                val result = withContext(Dispatchers.IO) {
                    repository.getCoursesWithRole(bearerToken)
                }
                result.fold(
                    onSuccess = { courseResponse ->
                        Log.d("CourseViewModel", "Successfully fetched courses: $courseResponse")
                        _courses.value = UIState.Success(courseResponse.courses)
                    },
                    onFailure = { error ->
                        Log.e("CourseViewModel", "Failed to fetch courses: ${error.message}", error)
                        _courses.value = UIState.Error("Failed to fetch courses: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Exception occurred while fetching courses: ${e.message}", e)
                _courses.value = UIState.Error("Exception occurred while fetching courses: ${e.message}", e)
            } finally {
                _isFetchingCourses.value = false
            }
        }
    }

    fun createCourseWithExtras(
        token: String,
        title: String,
        description: String,
        assignedUsers: List<Int>,
        file: MultipartBody.Part?
    ) {
        _courseCreationResult.value = UIState.Loading
        viewModelScope.launch {
            try {
                Log.d("CourseViewModel", "Creating course with title: $title, description: $description, assignedUsers: $assignedUsers, file: $file")
                val result = withContext(Dispatchers.IO) {
                    repository.createCourseWithExtras(
                        token = "Bearer $token",
                        title = title,
                        description = description,
                        assignedUsers = assignedUsers,
                        file = file
                    )
                }
                _courseCreationResult.value = result.fold(
                    onSuccess = {
                        Log.d("CourseViewModel", "Course created: ${it.title}")
                        fetchCoursesWithRole(token)
                        UIState.Success(it)
                    },
                    onFailure = { error ->
                        Log.e("CourseViewModel", "Error creating course: ${error.message}", error)
                        UIState.Error("Error creating course: ${error.message}", error)
                    }
                )

            } catch (e: Exception) {
                Log.e("CourseViewModel", "Exception during course creation: ${e.message}", e)
                _courseCreationResult.value = UIState.Error("Exception during course creation: ${e.message}", e)
            }
        }
    }

    fun deleteCourse(courseId: String, token: String) {
        viewModelScope.launch {
            try {
                Log.d("CourseViewModel", "Deleting course with id: $courseId")
                val result = withContext(Dispatchers.IO) {
                    repository.deleteCourse(courseId,token)
                }
                if (result.isSuccess) {
                    Log.d("CourseViewModel", "Course deleted successfully")
                    fetchCoursesWithRole(token) //refresh the list
                } else {
                    result.onFailure {
                        Log.e("CourseViewModel", "Failed to delete course: ${it.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Exception while deleting course: ${e.message}", e)
            }
        }
    }

    fun clearCourseCreationResult() {
        _courseCreationResult.value = null
    }

    /**
     * Updates the students enrolled in a specific course.
     *
     * @param courseId The ID of the course to update.
     * @param token The authentication token.
     * @param students A list of Student objects to add or remove from the course.
     */
    fun updateCourseStudents(courseId: String, token: String, students: List<Student>) {
        viewModelScope.launch {
            try {
                val studentIds = students.map { it.id }
                val updateCourseStudentsRequest = UpdateCourseStudentsRequest(studentIds = studentIds)
                Log.d("CourseViewModel", "Updating students for courseId: $courseId, with studentIds: $studentIds")

                val result = withContext(Dispatchers.IO) {
                    repository.updateCourseStudents(courseId, "Bearer $token", updateCourseStudentsRequest)
                }

                result.fold(
                    onSuccess = { updatedCourse ->
                        Log.d("CourseViewModel", "Successfully updated students for course $courseId")
                        _selectedCourse.value = UIState.Success(updatedCourse)
                        fetchCoursesWithRole(token) // Refresh the list of courses.
                    },
                    onFailure = { error ->
                        Log.e("CourseViewModel", "Failed to update students for course $courseId: ${error.message}", error)
                        _selectedCourse.value = UIState.Error("Failed to update students: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Exception while updating students for course $courseId: ${e.message}", e)
                _selectedCourse.value = UIState.Error("Exception while updating students for course $courseId: ${e.message}", e)
            }
        }
    }

    fun resetStudentCache() {
        Log.d("CourseViewModel", "Resetting student cache")
        _studentList.value = UIState.Loading
    }

    /**
     * Fetches the students for a specific course.
     *
     * @param courseId The ID of the course.
     * @param token The authentication token.
     */
    fun getStudentsByCourseId(courseId: String, token: String) {
        viewModelScope.launch {
            _studentList.value = UIState.Loading
            try {
                val bearerToken = token
                Log.d("CourseViewModel", "Fetching students with token: $bearerToken")
                Log.d("CourseViewModel", "Fetching students for course ID: $courseId")
                val result = withContext(Dispatchers.IO) {
                    repository.getStudentsByCourseId(courseId, bearerToken)
                }
                result.fold(
                    onSuccess = { students ->
                        Log.d("CourseViewModel", "Successfully fetched ${students.size} students for course $courseId")
                        _studentList.value = UIState.Success(students)
                    },
                    onFailure = { error ->
                        Log.e("CourseViewModel", "Failed to fetch students for course $courseId: ${error.message}", error)
                        _studentList.value = UIState.Error("Failed to fetch students for course $courseId: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Exception in getStudentsByCourseId: ${e.message}", e)
                _studentList.value = UIState.Error("Exception: ${e.message}", e)
            }
        }
    }

    /**
     * Deletes a student from a course.
     *
     * @param courseId The ID of the course.
     * @param token The authentication token.
     * @param studentId The ID of the student to delete.
     */
    fun deleteStudentFromCourse(courseId: String, token: String, studentId: Int) {
        viewModelScope.launch {
            try {
                Log.d("CourseViewModel", "Deleting student $studentId from course $courseId")
                val result = withContext(Dispatchers.IO) {
                    repository.deleteStudentFromCourse(courseId, "Bearer $token", studentId)
                }
                result.fold(
                    onSuccess = {
                        Log.d("CourseViewModel", "Successfully deleted student $studentId from course $courseId")
                        // Optionally, you might want to refresh the student list for the course
                        getStudentsByCourseId(courseId, token)
                        fetchCoursesWithRole(token) // Refresh courses list
                    },
                    onFailure = { error ->
                        Log.e("CourseViewModel", "Failed to delete student $studentId from course $courseId: ${error.message}", error)
                        _studentList.value = UIState.Error("Failed to delete student: ${error.message}", error) //Or _selectedCourse
                    }
                )
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Exception while deleting student $studentId from course $courseId: ${e.message}", e)
                _studentList.value = UIState.Error("Exception: ${e.message}", e)
            }
        }
    }

    fun addStudentsToCourse(courseId: String,token: String, students: List<Student>){
        viewModelScope.launch {
            try {
                val studentIds = students.map{it.id}
                val updateCourseStudentsRequest = UpdateCourseStudentsRequest(studentIds = studentIds)
                Log.d("CourseViewModel", "Adding students: $studentIds to course: $courseId")
                val result = withContext(Dispatchers.IO){
                    repository.updateCourseStudents(courseId, "Bearer $token", updateCourseStudentsRequest)
                }

                result.fold(
                    onSuccess = { updatedCourse ->
                        Log.d("CourseViewModel", "Successfully added students to course $courseId")
                        _selectedCourse.value = UIState.Success(updatedCourse)
                        getStudentsByCourseId(courseId, token)
                        fetchCoursesWithRole(token) // Refresh the list of courses.
                    },
                    onFailure = { error ->
                        Log.e("CourseViewModel", "Failed to add students for course $courseId: ${error.message}", error)
                        _selectedCourse.value = UIState.Error("Failed to add students: ${error.message}", error)
                    }
                )

            } catch (e: Exception){
                Log.e("CourseViewModel", "Exception while adding students to the course ${e.message}", e)
                _selectedCourse.value = UIState.Error("Exception while adding students to the course ${e.message}", e)
            }

        }
    }

    fun getMyCourses(token: String) {
        viewModelScope.launch {
            _courses.value = UIState.Loading // Set loading state
            try {
                Log.d("CourseViewModel", "Fetching courses for student")
                val result = withContext(Dispatchers.IO) {
                    repository.getMyCourses(token)
                }

                result.fold(
                    onSuccess = { courseResponse ->
                        Log.d("CourseViewModel", "Successfully fetched student courses")
                        _courses.value = UIState.Success(courseResponse.courses)
                    },
                    onFailure = { error ->
                        Log.e("CourseViewModel", "Failed to fetch student courses: ${error.message}")
                        _courses.value = UIState.Error("Failed to fetch your courses: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Exception while fetching student courses: ${e.message}", e)
                _courses.value = UIState.Error("Exception: ${e.message}", e)
            }
        }
    }

    fun loadCourseResources(courseId: String, token: String) {
        viewModelScope.launch {
            _courseResources.value = UIState.Loading
            val result = repository.getCourseResources(courseId, token)

            result
                .onSuccess { resourcesList ->
                    _courseResources.value = UIState.Success(resourcesList)
                }
                .onFailure { throwable ->
                    _courseResources.value = UIState.Error(
                        message = throwable.message ?: "Unknown error loading resources",
                        error = throwable // Pass the Throwable object here
                    )
                    Log.e("CourseViewModel", "Error loading resources: ${throwable.message}", throwable)
                }
        }
    }

    fun addCourseResource(
        courseId: String,
        token: String,
        title: String,
        description: String?,
        type: String, // e.g., "document"
        friendlyType: String, // e.g., "PDF"
        url: String?,
        content: String?,
        tags: List<String>?,
        filePart: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull()) // already backend type
            val friendlyTypePart = friendlyType.toRequestBody("text/plain".toMediaTypeOrNull()) // NEW
            val urlPart = url?.toRequestBody("text/plain".toMediaTypeOrNull())
            val contentPart = content?.toRequestBody("text/plain".toMediaTypeOrNull())
            val tagsPart = tags?.joinToString(",")?.toRequestBody("text/plain".toMediaTypeOrNull())

            val result = repository.createCourseResource(
                courseId = courseId,
                token = token,
                title = titlePart,
                description = descriptionPart,
                type = typePart,
                friendlyType = friendlyTypePart, // NEW
                url = urlPart,
                content = contentPart,
                tags = tagsPart,
                file = filePart
            )

            result
                .onSuccess {
                    loadCourseResources(courseId, token)
                }
                .onFailure { throwable ->
                    Log.e("AddResourceError", "Failed to add resource: ${throwable.message}")
                }
        }
    }


    fun updateCourseResource(
        resourceId: String,
        courseId: String,
        token: String,
        title: String?,
        description: String?,
        type: String?,
        friendlyType: String?,
        url: String?,
        content: String?,
        tags: List<String>?,
        filePart: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            val titlePart = title?.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = type?.toRequestBody("text/plain".toMediaTypeOrNull())
            val friendlyTypePart = friendlyType?.toRequestBody("text/plain".toMediaTypeOrNull())
            val urlPart = url?.toRequestBody("text/plain".toMediaTypeOrNull())
            val contentPart = content?.toRequestBody("text/plain".toMediaTypeOrNull())
            val tagsPart = tags?.joinToString(",")?.toRequestBody("text/plain".toMediaTypeOrNull())

            repository.updateCourseResource(
                resourceId = resourceId,
                token = token,
                title = titlePart,
                description = descriptionPart,
                type = typePart,
                friendlyType = friendlyTypePart,
                url = urlPart,
                content = contentPart,
                tags = tagsPart,
                file = filePart
            ).onSuccess {
                loadCourseResources(courseId = courseId, token = token)
            }.onFailure { throwable ->
                Log.e("UpdateResourceError", "Failed to update resource: ${throwable.message}")
            }
        }
    }


    fun deleteCourseResource(resourceId: String, token: String, courseId: String) { // Added courseId to refresh
        viewModelScope.launch {
            repository.deleteCourseResource(resourceId, token)
                .onSuccess {
                    loadCourseResources(courseId, token) // Refresh list
                }
                .onFailure { throwable ->
                    Log.e("DeleteResourceError", "Failed to delete resource: ${throwable.message}")
                }
        }
    }


    class CourseViewModelFactory(
        private val repository: LMSRepository,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            if (modelClass.isAssignableFrom(CourseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CourseViewModel(repository, handle) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
