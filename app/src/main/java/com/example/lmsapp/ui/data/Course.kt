package com.example.lmsapp.ui.data

import com.example.lmsapp.ui.data.DataClasses.Student

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val fileUrl: String? = null,
    val createdBy: Int,
    val createdAt: String,
    val updatedAt: String,
    val teacherId: Int,
    val students: List<Student>
)

