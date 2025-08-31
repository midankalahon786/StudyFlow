package com.example.lmsapp.ui.data.DataClasses

data class Submission(
    val studentId: Int,
    val quizId: Int,
    val answers: List<String>,  // List of answers selected by the student
    val score: Int,
    val submittedAt: String
)