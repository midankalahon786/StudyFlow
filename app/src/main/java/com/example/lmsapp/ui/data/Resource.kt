package com.example.lmsapp.ui.data.DataClasses

import ResourceType
import com.example.lmsapp.ui.data.UserInfo
import kotlinx.serialization.Serializable

@Serializable
data class Resource(
    val resourceId: String,
    val courseId: String?,
    val teacherId: String?,
    val title: String,
    val description: String?,
    val type: ResourceType?,
    val friendlyType: String? = null,
    val url: String?, // For video links, file URLs
    val fileName: String?, // For files
    val originalName: String?,
    val filePath: String?,
    val fileSize: Long?,
    val uploadDate: String?,
    val content: String?, // For notes directly stored
    val tags: List<String>?, // For filtering
    val createdAt: String, // Use String for simplicity, parse to LocalDateTime if needed for display
    val uploadedBy: UserInfo?, // Or just userId and fetch user info
    val teacherName: String? = null
)

