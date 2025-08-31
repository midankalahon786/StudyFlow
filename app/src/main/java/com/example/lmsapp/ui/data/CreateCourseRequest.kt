package com.example.lmsapp.ui.data

data class CreateCourseRequest(
    val title: String,
    val description: String,
    val assignedStudents: List<String>,
    val file: String? = null
)