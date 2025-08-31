package com.example.lmsapp.ui.data

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: Int,
    val questionText: String, // Rename 'text' to 'questionText'
    val options: List<String>,
    val correctOptionIndex: Int, // Rename 'correctAnswer' to 'correctOptionIndex' and keep as String initially
    val mark: Int
)