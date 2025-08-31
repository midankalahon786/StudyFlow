package com.example.lmsapp.ui.data.DataClasses

data class StudentRegisterRequest(
    val username: String,
    val password: String,
    val role: String = "student",
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val enrollmentNumber: String,
    val department: String,
    val semester: Int,
    val batchYear: Int
)
