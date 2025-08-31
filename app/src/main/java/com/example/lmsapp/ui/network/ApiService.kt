package com.example.lmsapp.ui.network

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
import com.example.lmsapp.ui.data.DataClasses.QuizResponse
import com.example.lmsapp.ui.data.DataClasses.Report
import com.example.lmsapp.ui.data.DataClasses.Resource
import com.example.lmsapp.ui.data.DataClasses.StudentResponse
import com.example.lmsapp.ui.data.DataClasses.StudentRegisterRequest
import com.example.lmsapp.ui.data.DataClasses.SubmissionSummary
import com.example.lmsapp.ui.data.DataClasses.SubmitQuizRequest
import com.example.lmsapp.ui.data.DataClasses.SubmitQuizResponse
import com.example.lmsapp.ui.data.DataClasses.TeacherRegisterRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.PUT

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("auth/register/student")
    suspend fun registerStudent(@Body request: StudentRegisterRequest): Response<Unit>

    @POST("auth/register/teacher")
    suspend fun registerTeacher(@Body request: TeacherRegisterRequest): Response<Unit>

    @GET("courses/list")
    suspend fun getCourses(@Header("Authorization") token: String): Response<CourseResponse>

    @DELETE("courses/delete/{courseId}")
    suspend fun deleteCourse(@Path("courseId") courseId: String, @Header("Authorization") token: String): Response<Unit>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest, @Header("Authorization") token: String): Response<ChangePasswordResponse>

    @POST("quizzes/create")
    suspend fun createQuiz(@Header("Authorization") token: String, @Body request: CreateQuizRequest): Response<QuizResponse>

    @GET("quizzes/list")
    suspend fun getQuizzes(@Header("Authorization") token: String): Response<List<Quiz>>

    @GET("quizzes/student-quizzes")
    suspend fun getQuizzesForStudent(@Header("Authorization") token: String): Response<List<Quiz>>

    @GET("quizzes/{id}")
    suspend fun getQuizById(
        @Header("Authorization") token: String,
        @Path("id") quizId: Int?
    ): Response<Quiz>

    @POST("quizzes/submit")
    suspend fun submitQuiz(
        @Header("Authorization") token: String,
        @Body request: SubmitQuizRequest
    ): Response<SubmitQuizResponse>

    @GET("courses/students")
    suspend fun getStudents(@Header("Authorization") token: String): Response<StudentResponse>

    @GET("courses/{courseId}/students")  // New route to get students by course ID
    suspend fun getCourseStudents(
        @Path("courseId") courseId: String,
        @Header("Authorization") token: String
    ): Response<StudentResponse>


    @Multipart
    @POST("courses/create-with-extras")
    suspend fun createCourseWithExtras(@Header("Authorization") token: String, @Part("title") title: RequestBody, @Part("description") description: RequestBody, @Part("assignedUsers") assignedUsers: RequestBody, @Part file: MultipartBody.Part? = null): Response<Course>

    // Added route for updating course students
    @PUT("courses/{courseId}/students")
    suspend fun updateCourseStudents(
        @Path("courseId") courseId: String,
        @Header("Authorization") token: String,
        @Body request: UpdateCourseStudentsRequest
    ): Response<Course>

    @DELETE("courses/{courseId}/students/{studentId}")
    suspend fun deleteStudentFromCourse(
        @Path("courseId") courseId: String,
        @Header("Authorization") token: String,
        @Path("studentId") studentId: Int
    ): Response<Unit>

    @GET("courses/my-courses")
    suspend fun getMyCourses(@Header("Authorization") token: String): Response<CourseResponse>

    @GET("courses/{courseId}/resources")
    suspend fun getCourseResources(
        @Path("courseId") courseId: String,
        @Header("Authorization") token: String
    ): Response<List<Resource>>

    @Multipart // IMPORTANT: This annotation is required for multipart requests
    @POST("courses/{courseId}/resources/upload")
    suspend fun createCourseResource(
        @Path("courseId") courseId: String,
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("type") type: RequestBody,
        @Part("friendlyType") friendlyType: RequestBody?,
        @Part("url") url: RequestBody?,
        @Part("content") content: RequestBody?,
        @Part("tags") tags: RequestBody?,
        @Part file: MultipartBody.Part? // This will carry your actual file
    ): Response<Resource> // Or directly Resource if you handle errors outside of Result in Repository


    @Multipart // IMPORTANT: This annotation is required for multipart requests
    @PUT("courses/{resourceId}") // Assuming your PUT endpoint for resources looks like this
    suspend fun updateCourseResource(
        @Path("resourceId") resourceId: String,
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("type") type: RequestBody?,
        @Part("friendlyType") friendlyType: RequestBody?,
        @Part("url") url: RequestBody?,
        @Part("content") content: RequestBody?,
        @Part("tags") tags: RequestBody?,
        @Part file: MultipartBody.Part? // This will carry your actual file
    ): Response<Resource> // Or directly Resource

    @DELETE("courses/{resourceId}/resources")
    suspend fun deleteCourseResource(
        @Path("resourceId") resourceId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("quizzes/{quizId}/report/{studentId}")
    suspend fun getQuizReport(
        @Header("Authorization") token: String,
        @Path("quizId") quizId: Int,
        @Path("studentId") studentId: Int
    ):Response<Report>

    @GET("quizzes/student/{studentId}/submissions")
    suspend fun getAllStudentSubmissions(
        @Header("Authorization") token: String,
        @Path("studentId") studentId: Int
    ): Response<List<SubmissionSummary>>

    @DELETE("quizzes/{quizId}")
    suspend fun deleteQuiz(
        @Header("Authorization") token: String,
        @Path("quizId") quizId: Int
    ): Response<Unit>

    @PUT("quizzes/{quizId}")
    suspend fun updateQuiz(
        @Header("Authorization") token: String,
        @Path("quizId") quizId: Int,
        @Body updatedQuiz: Quiz // Send the entire updated quiz object
    ): Response<Quiz>

    @GET("discussion/courses/{courseId}/comments")
    suspend fun getCourseComments(
        @Header("Authorization") token: String,
        @Path("courseId") courseId: String
    ): Response<List<Comment>>

    @POST("discussion/comments")
    suspend fun createComment(
        @Header("Authorization") authToken: String, // Add Authorization header
        @Body request: CreateCommentRequest
    ): Response<Comment>


    @PUT("discussion/comments/{id}")
    suspend fun updateComment(
        @Header("Authorization") authToken: String, // Add Authorization header
        @Path("id") commentId: String,
        @Body request: CreateCommentRequest
    ): Response<Comment>

    @DELETE("discussion/comments/{id}")
    suspend fun deleteComment(
        @Header("Authorization") authToken: String, // Add Authorization header
        @Path("id") commentId: String
    ): Response<Unit>


}

