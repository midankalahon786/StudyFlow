package com.example.lmsapp.ui.network

import android.util.Log
import com.example.lmsapp.ui.data.AnswerDetail
import com.example.lmsapp.ui.data.ChangePasswordRequest
import com.example.lmsapp.ui.data.ChangePasswordResponse
import com.example.lmsapp.ui.data.Comment
import com.example.lmsapp.ui.data.Course
import com.example.lmsapp.ui.data.CourseResponse
import com.example.lmsapp.ui.data.CreateCommentRequest
import com.example.lmsapp.ui.data.CreateQuizRequest
import com.example.lmsapp.ui.data.LoginRequest
import com.example.lmsapp.ui.data.LoginResponse
import com.example.lmsapp.ui.data.DataClasses.Quiz
import com.example.lmsapp.ui.data.DataClasses.Report
import com.example.lmsapp.ui.data.DataClasses.Resource
import com.example.lmsapp.ui.data.DataClasses.Student
import com.example.lmsapp.ui.data.DataClasses.StudentRegisterRequest
import com.example.lmsapp.ui.data.DataClasses.SubmissionSummary
import com.example.lmsapp.ui.data.DataClasses.SubmitQuizRequest
import com.example.lmsapp.ui.data.DataClasses.SubmitQuizResponse
import com.example.lmsapp.ui.data.DataClasses.TeacherRegisterRequest
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import com.google.gson.annotations.SerializedName
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.IOException


// Data class to represent the request body for updating course students
data class UpdateCourseStudentsRequest(
    @SerializedName("studentIds") val studentIds: List<Int>
)

open class LMSRepository() {

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.api.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    Log.d("LoginResponse", "Login successful: $body")
                    Result.success(it) // Return the entire LoginResponse object
                } ?: Result.failure(Exception("Null response body"))
            } else {
                logError("LoginResponse", response)
                Result.failure(Exception("Login failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("LoginResponse", e)
            Result.failure(e)
        }
    }

    suspend fun registerStudent(request: StudentRegisterRequest): Result<Unit> {
        return try {
            val response = RetrofitClient.api.registerStudent(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                logError("RegisterStudent", response)
                Result.failure(Exception("Student registration failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("RegisterStudent", e)
            Result.failure(e)
        }
    }

    suspend fun registerTeacher(request: TeacherRegisterRequest): Result<Unit> {
        return try {
            val response = RetrofitClient.api.registerTeacher(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                logError("RegisterTeacher", response)
                Result.failure(Exception("Teacher registration failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("RegisterTeacher", e)
            Result.failure(e)
        }
    }

    suspend fun changePassword(token: String, oldPass: String, newPass: String): Result<ChangePasswordResponse> {
        return try {
            val requestBody = ChangePasswordRequest(oldPass, newPass)
            Log.d("LMSRepository", "Change password request with token: Bearer $token")
            val response = RetrofitClient.api.changePassword(requestBody, token)

            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    Log.d("ChangePassword", "Password changed successfully: ${it.message}")
                    Result.success(it)
                } ?: Result.failure(Exception("Change password successful but body is null"))
            } else {
                logError("ChangePasswordError", response)
                Result.failure(Exception("Failed to change password: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("ChangePasswordException", e)
            Result.failure(e)
        }
    }


    suspend fun createQuiz(token: String, request: CreateQuizRequest): Result<Unit> {
        return try {
            val response = RetrofitClient.api.createQuiz(token, request)
            Log.d("LMSRepository", "API Response: Code ${response.code()} - ${response.message()}")
            if (response.isSuccessful) {
                Log.d("CreateQuiz", "Quiz created successfully")
                Result.success(Unit)
            } else {
                logError("CreateQuizError", response)
                Result.failure(Exception("Failed to create quiz: ${response.errorBody()?.string()}"))

            }
        } catch (e: Exception) {
            logError("CreateQuizException", e)
            Result.failure(e)
        }
    }


    suspend fun getQuizById(token: String, quizId: Int?): Result<Quiz> {
        return try {
            // The ApiService expects the token with "Bearer " prefix here too,
            // based on the @Header annotation in your ApiService.
            val response = RetrofitClient.api.getQuizById(token, quizId)
            if (response.isSuccessful) {
                response.body()?.let { quiz ->
                    Log.d("GetQuizByIdSuccess", "Fetched quiz: $quiz")
                    Result.success(quiz)
                } ?: Result.failure(Exception("Quiz response body is null"))
            } else {
                logError("GetQuizByIdError", response)
                Result.failure(Exception("Error fetching quiz: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("GetQuizByIdException", e)
            Result.failure(e)
        }
    }


    suspend fun getAllQuizzes(token: String): Result<List<Quiz>> {
        return try {
            // The ApiService expects the token with "Bearer " prefix here too.
            val response = RetrofitClient.api.getQuizzes(token)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to fetch quizzes"))
            }
        } catch (e: Exception) {
            logError("GetQuizzesException", e)
            Result.failure(e)
        }
    }


    suspend fun getStudents(token: String): Result<List<Student>> {
        return try {
            Log.d("LMSRepository", "getStudents called with token: $token")
            val response = RetrofitClient.api.getStudents(token)
            Log.d("LMSRepository", "getStudents response code: ${response.code()}")
            Log.d("LMSRepository", "getStudents response message: ${response.message()}")
            Log.d("LMSRepository", "getStudents response body: ${response.body()}")
            Log.d("LMSRepository", "getStudents response headers: ${response.headers()}")


            if (response.isSuccessful) {
                val students = response.body()?.students ?: emptyList()
                Log.d("LMSRepository", "Successfully fetched students: $students")
                Result.success(students)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("LMSRepository", "Failed to fetch students. Code: ${response.code()}, Body: $errorBody")
                Result.failure(Exception("Failed to fetch students: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("LMSRepository", "Exception in getStudents: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun createCourseWithExtras(token: String, title: String, description: String, assignedUsers: List<Int>, file: MultipartBody.Part?): Result<Course> {
        return try {
            Log.d("LMSRepository", "createCourseWithExtras called with token: $token")
            val assignedUsersJson = Gson().toJson(assignedUsers) // Convert to JSON string
            val assignedUsersBody = assignedUsersJson.toRequestBody("application/json".toMediaTypeOrNull())

            // Ensure "Bearer " prefix is used here for the token
            val response: Response<Course> = RetrofitClient.api.createCourseWithExtras(
                token = token,
                title = title.toRequestBody("text/plain".toMediaTypeOrNull()),
                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                assignedUsers = assignedUsersBody,
                file = file
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d("CreateCourseExtras", "Course created successfully: $it")
                    Result.success(it)
                } ?: Result.failure(Exception("Course creation successful but body is null"))
            } else {
                logError("CreateCourseExtrasError", response)
                Result.failure(Exception("Failed to create course with extras: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("CreateCourseExtrasException", e)
            Result.failure(e)
        }
    }

    suspend fun getCoursesWithRole(token: String): Result<CourseResponse> {
        return try {
            Log.d("LMSRepository", "Fetching courses with token: $token")
            // Ensure "Bearer " prefix is used here for the token
            val response = RetrofitClient.api.getCourses(token)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to fetch courses: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCourse(courseId: String, token: String): Result<Unit> {
        return try {
            Log.d("LMSRepository", "Deleting course with id: $courseId, token: $token")
            // Ensure "Bearer " prefix is used here for the token
            val response = RetrofitClient.api.deleteCourse(courseId, token)
            if (response.isSuccessful) {
                Log.d("LMSRepository", "Course deleted successfully")
                Result.success(Unit)
            } else {
                logError("DeleteCourseError", response)
                Result.failure(Exception("Failed to delete course: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("DeleteCourseException", e)
            Result.failure(e)
        }
    }

    suspend fun updateCourseStudents(courseId: String, token: String, request: UpdateCourseStudentsRequest): Result<Course> {
        return try {
            Log.d("UpdateCourseStudents","Token Received:$token")
            // Ensure "Bearer " prefix is used here for the token
            val response = RetrofitClient.api.updateCourseStudents(courseId, token, request)
            if (response.isSuccessful) {
                response.body()?.let { updatedCourse ->
                    Log.d("UpdateCourseStudents", "Successfully updated students for course $courseId.  Response Body: $updatedCourse")
                    Result.success(updatedCourse)
                } ?: Result.failure(Exception("Successful update, but course data is null"))
            } else {
                logError("UpdateCourseStudentsError", response)
                Result.failure(Exception("Failed to update students for course $courseId: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("UpdateCourseStudentsException", e)
            Result.failure(e)
        }
    }

    suspend fun getStudentsByCourseId(courseId: String, token: String): Result<List<Student>> {
        return try {
            // Ensure "Bearer " prefix is used here for the token
            val response = RetrofitClient.api.getCourseStudents(courseId, token)
            Log.d("LMS Repository", "Fetching students by course id: $courseId and token: $token")
            if (response.isSuccessful) {
                val studentResponse = response.body()
                val students = studentResponse?.students ?: emptyList()
                Result.success(students)
            } else {
                Result.failure(Exception("Failed to fetch students for course: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStudentFromCourse(courseId: String, token: String, studentId: Int): Result<Unit> {
        return try {
            Log.d("LMSRepository", "Deleting student $studentId from course $courseId with token: $token")
            // Ensure "Bearer " prefix is used here for the token
            val response = RetrofitClient.api.deleteStudentFromCourse(courseId, token, studentId)
            if (response.isSuccessful) {
                Log.d("LMSRepository", "Successfully deleted student $studentId from course $courseId")
                Result.success(Unit)
            } else {
                logError("DeleteStudentFromCourseError", response)
                Result.failure(Exception("Failed to delete student $studentId from course $courseId: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("DeleteStudentFromCourseException", e)
            Result.failure(e)
        }
    }

    suspend fun getMyCourses(token: String): Result<CourseResponse> {
        return try {
            Log.d("LMSRepository", "Fetching courses for student with token: $token")
            val response = RetrofitClient.api.getMyCourses(token) // Ensure the token is passed correctly
            if (response.isSuccessful) {
                val courseResponse = response.body()
                Log.d("LMSRepository", "Successfully fetched student courses: $courseResponse")
                if (courseResponse != null) {
                    Result.success(courseResponse)
                } else {
                    Result.failure(Exception("Empty or null course response"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("LMSRepository", "Failed to fetch student courses. Code: ${response.code()}, Body: $errorBody")
                Result.failure(Exception("Failed to fetch student courses: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("LMSRepository", "Exception in getMyCourses: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun submitQuiz(token: String, quizId: Int, studentAnswers: Map<Int, String>, score: Int, studentId: Int): Result<SubmitQuizResponse> {
        return try {
            val answersList = studentAnswers.map { (questionId, selectedOption) ->
                AnswerDetail(questionId, selectedOption)
            }
            val request = SubmitQuizRequest(quizId, answersList, score, studentId)
            Log.d("LMSRepository", "Submitting quiz with request: $request")

            // IMPORTANT CHANGE HERE:
            val response = RetrofitClient.api.submitQuiz("Bearer $token", request)

            if (response.isSuccessful) {
                // If the HTTP call was successful (2xx status code)
                val submitQuizResponse = response.body()
                if (submitQuizResponse != null) {
                    Result.success(submitQuizResponse)
                } else {
                    // This case means a 2xx response but with an empty body, which might be an API error
                    val errorMessage = "Submit quiz successful, but response body is null."
                    Log.e("SubmitQuizError", errorMessage)
                    Result.failure(RuntimeException(errorMessage))
                }
            } else {
                // If the HTTP call was not successful (e.g., 4xx or 5xx status code)
                val errorBody = response.errorBody()?.string()
                val errorMessage = "Quiz submission failed: ${response.code()} - ${errorBody ?: response.message()}"
                Log.e("SubmitQuizError", errorMessage)
                Result.failure(HttpException(response)) // Use HttpException for HTTP errors
            }
        } catch (e: IOException) {
            // Network error (no internet, timeout, etc.)
            val errorMessage = "Network error during quiz submission: ${e.message}"
            Log.e("SubmitQuizError", errorMessage, e)
            Result.failure(RuntimeException("Network error: Please check your internet connection."))
        } catch (e: Exception) {
            // Other unexpected errors
            val errorMessage = "Unexpected error during quiz submission: ${e.message}"
            Log.e("SubmitQuizError", errorMessage, e)
            Result.failure(RuntimeException("An unexpected error occurred."))
        }
    }

    suspend fun getQuizReport(token: String, quizId: Int, studentId: Int): Result<Report> {
        return try {
            val response = RetrofitClient.api.getQuizReport("Bearer $token", quizId, studentId)
            if (response.isSuccessful) {
                val report = response.body()
                if (report != null) {
                    Result.success(report)
                } else {
                    Result.failure(RuntimeException("Failed to fetch report: Response body is null."))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(RuntimeException("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllStudentReports(token: String, studentId: Int): Result<List<SubmissionSummary>> {
        return try {
            val response = RetrofitClient.api.getAllStudentSubmissions("Bearer $token", studentId)
            if (response.isSuccessful) {
                val reports = response.body()
                if (reports != null) {
                    Result.success(reports)
                } else {
                    Result.failure(RuntimeException("Failed to fetch all reports: Response body is null."))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(RuntimeException("Failed to fetch all reports: ${response.code()} - ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteQuiz(token: String, quizId: Int): Result<Unit> {
        return try {
            val response = RetrofitClient.api.deleteQuiz(token, quizId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error during quiz deletion."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateQuiz(token: String, quizId: Int, updatedQuiz: Quiz): Result<Quiz> {
        return try {
            val response = RetrofitClient.api.updateQuiz(token, quizId, updatedQuiz)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error during quiz update."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCourseComments(courseId: String, token: String): Result<List<Comment>> {
        return try {
            val response = RetrofitClient.api.getCourseComments(token,courseId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error fetching course comments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createComment(token: String, content: String, courseId: String? = null, parentId: String? = null): Result<Comment> {
        return try {
            val request = CreateCommentRequest(content, courseId, parentId)
            val response = RetrofitClient.api.createComment(token, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error creating comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateComment(token: String, commentId: String, content: String): Result<Comment> {
        return try {
            val request = CreateCommentRequest(content = content) // Only content is updated
            // Pass the authToken directly to the API service call
            val response = RetrofitClient.api.updateComment("Bearer $token", commentId, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error updating comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(token: String, commentId: String): Result<Unit> {
        return try {
            // Pass the authToken directly to the API service call
            val response = RetrofitClient.api.deleteComment("Bearer $token", commentId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error deleting comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCourseResources(courseId: String, token: String): Result<List<Resource>> {
        return try {
            Log.d("LMSRepository", "Fetching resources for courseId: $courseId with token: $token")
            val response = RetrofitClient.api.getCourseResources(courseId, token)
            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d("LMSRepository", "Successfully fetched resources: $it")
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body for resources"))
            } else {
                logError("GetCourseResourcesError", response)
                Result.failure(Exception("Failed to fetch course resources: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("GetCourseResourcesException", e)
            Result.failure(e)
        }
    }

    // MODIFIED: createCourseResource to accept individual RequestBody parts and MultipartBody.Part
    suspend fun createCourseResource(
        courseId: String,
        token: String,
        title: RequestBody,
        description: RequestBody?,
        type: RequestBody,
        friendlyType: RequestBody,
        url: RequestBody?,
        content: RequestBody?,
        tags: RequestBody?,
        file: MultipartBody.Part?
    ): Result<Resource> {
        return try {
            Log.d("LMSRepository", "Creating resource for courseId: $courseId")

            val response = RetrofitClient.api.createCourseResource(
                courseId = courseId,
                token = token,
                title = title,
                description = description,
                type = type,
                friendlyType = friendlyType,
                url = url,
                content = content,
                tags = tags,
                file = file
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d("LMSRepository", "Resource created successfully: $it")
                    Result.success(it)
                } ?: Result.failure(Exception("Resource creation successful but body is null"))
            } else {
                logError("CreateCourseResourceError", response)
                Result.failure(
                    Exception(
                        "Failed to create course resource: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                )
            }
        } catch (e: Exception) {
            logError("CreateCourseResourceException", e)
            Result.failure(e)
        }
    }


    suspend fun updateCourseResource(
        resourceId: String,
        token: String,
        title: RequestBody?,
        description: RequestBody?,
        type: RequestBody?,
        friendlyType: RequestBody?, // ✅ NEW
        url: RequestBody?,
        content: RequestBody?,
        tags: RequestBody?,
        file: MultipartBody.Part?
    ): Result<Resource> {
        return try {
            Log.d("LMSRepository", "Updating resourceId: $resourceId")

            val response = RetrofitClient.api.updateCourseResource(
                resourceId = resourceId,
                token = token,
                title = title,
                description = description,
                type = type,
                friendlyType = friendlyType, // ✅ Pass it here
                url = url,
                content = content,
                tags = tags,
                file = file
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d("LMSRepository", "Resource updated successfully: $it")
                    Result.success(it)
                } ?: Result.failure(Exception("Resource update successful but body is null"))
            } else {
                logError("UpdateCourseResourceError", response)
                Result.failure(
                    Exception("Failed to update course resource: ${response.code()} - ${response.errorBody()?.string()}")
                )
            }
        } catch (e: Exception) {
            logError("UpdateCourseResourceException", e)
            Result.failure(e)
        }
    }



    suspend fun deleteCourseResource(resourceId: String, token: String): Result<Unit> {
        return try {
            Log.d("LMSRepository", "Deleting resourceId: $resourceId")
            val response = RetrofitClient.api.deleteCourseResource(resourceId, token)
            if (response.isSuccessful) {
                Log.d("LMSRepository", "Resource deleted successfully")
                Result.success(Unit)
            } else {
                logError("DeleteCourseResourceError", response)
                Result.failure(Exception("Failed to delete course resource: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            logError("DeleteCourseResourceException", e)
            Result.failure(e)
        }
    }



    // Logging function to avoid repetition
    private fun logError(tag: String, e: Exception) {
        Log.e(tag, "Exception: ${e.message}")
    }

    // Logging function to log API responses
    private fun logError(tag: String, response: Response<*>) {
        Log.e(tag, "Error: ${response.errorBody()?.string()}")
    }
}