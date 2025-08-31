package com.example.lmsapp.ui.data.DataClasses

data class SubmitQuizResponse(
    val message: String,
    val submissionId: Int? = null,
    val score: Int? = null
)