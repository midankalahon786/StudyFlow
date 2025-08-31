package com.example.lmsapp.ui.data.DataClasses

import com.example.lmsapp.ui.data.Question

data class Quiz(
    val id: Int,
    val title: String,
    val timeLimit: Int,
    val totalMarks: Int,
    val negativeMarking: Float,
    val createdBy: String? = null,
    val questions: List<Question>
)
