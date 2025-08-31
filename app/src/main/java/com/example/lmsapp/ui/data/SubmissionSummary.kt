package com.example.lmsapp.ui.data.DataClasses

data class SubmissionSummary(
    val submissionId: Int,
    val quizId: Int,
    val quizTitle: String, // Assuming backend provides this
    val score: Int,
    val totalMarks: Int, // Assuming backend provides this
    val percentage: Double, // Assuming backend provides this
    val submittedAt: String // Or use a Date type if parsing
)