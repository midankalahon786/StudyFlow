package com.example.lmsapp.ui.data

import ResourceType

data class UpdateResourceRequest(
    val title: String?,
    val description: String?,
    val type: ResourceType?,
    val url: String?,
    val content: String?,
    val tags: List<String>?
)