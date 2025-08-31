package com.example.lmsapp.ui.data.DataClasses

data class TeacherRegisterRequest(
    val username: String,
    val password: String,
    val role: String = "teacher",
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val employeeId: String,
    val department: String,
    val designation: String,
    val yearsOfExperience: Int,
    val qualifications: String
)
