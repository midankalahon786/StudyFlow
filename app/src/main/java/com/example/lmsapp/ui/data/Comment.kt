package com.example.lmsapp.ui.data

data class Comment(
    val id: String,
    val content: String,
    val userId: String, // Author's ID
    val courseId: String?, // Nullable for replies if not directly associated with course
    val parentId: String?, // Nullable for top-level comments
    val createdAt: String, // Or use a proper Date/DateTime object
    val updatedAt: String, // Or use a proper Date/DateTime object
    val author: UserInfo, // Included author details
    val replies: List<Comment>? = null // Nested replies
)