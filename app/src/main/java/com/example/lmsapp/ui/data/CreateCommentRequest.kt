package com.example.lmsapp.ui.data

data class CreateCommentRequest(
    val content: String,
    val courseId: String? = null, // Optional for replies, required for top-level
    val parentId: String? = null // Optional for top-level comments
)