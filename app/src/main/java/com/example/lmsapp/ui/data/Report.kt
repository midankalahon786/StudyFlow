package com.example.lmsapp.ui.data.DataClasses

data class Report(
    val submissionId: Int,
    val quizId: Int,
    val quizTitle: String,
    val studentId: Int,
    val studentUsername: String,
    val score: Int,
    val totalMarks: Int,
    val percentage: Double,
    val questions: List<ReportQuestionDetail>
)