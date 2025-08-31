package com.example.lmsapp.ui.data.DataClasses

data class ReportQuestionDetail(
    val questionId: Int,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val studentSelectedOption: String?,
    val isCorrect: Boolean,
    val mark: Int
)