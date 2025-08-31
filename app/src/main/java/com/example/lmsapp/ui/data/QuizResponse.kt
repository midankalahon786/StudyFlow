package com.example.lmsapp.ui.data.DataClasses

import com.example.lmsapp.ui.data.Question

data class QuizResponse(
    val id: Int,
    val title: String,
    val timeLimit: Int,
    val negativeMarking: Boolean,
    val totalMarks: Int,
    val questions: List<Question>
)