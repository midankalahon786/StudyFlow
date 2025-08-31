package com.example.lmsapp.ui.data

import kotlinx.serialization.Serializable

@Serializable
data class CreateQuizRequest(
    val title: String,
    val timeLimit: Int,  // in minutes
    val negativeMarking: Float,
    val totalMarks: Int,
    val questions: List<Question>,
    val createdBy: Int,
)