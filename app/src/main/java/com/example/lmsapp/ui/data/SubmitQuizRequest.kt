package com.example.lmsapp.ui.data.DataClasses

import com.example.lmsapp.ui.data.AnswerDetail

data class SubmitQuizRequest(
    val quizId: Int,
    val answers: List<AnswerDetail>,
    val score: Int,
    val studentId: Int
)
