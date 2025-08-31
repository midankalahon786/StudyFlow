package com.example.lmsapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lmsapp.CourseDiscussionViewModel
import com.example.lmsapp.ui.data.Comment
import com.example.lmsapp.MainActivity
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import com.example.lmsapp.ui.network.formatDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionForumScreen(
    navController: NavController,
    courseId: String,
    sharedAuthViewModel: SharedAuthViewModel = viewModel()
) {
    val discussionViewModelFactory = remember {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CourseDiscussionViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return CourseDiscussionViewModel(
                        repository = (navController.context as MainActivity).lmsRepository,
                        sharedAuthViewModel = sharedAuthViewModel
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    val currentDiscussionViewModel: CourseDiscussionViewModel = viewModel(factory = discussionViewModelFactory)

    val topLevelComments by currentDiscussionViewModel.comments.collectAsState()
    val isLoading by currentDiscussionViewModel.isLoading.collectAsState()
    val errorMessage by currentDiscussionViewModel.errorMessage.collectAsState()

    var newMessageContent by remember { mutableStateOf("") }

    // Get the current user's ID to align their messages
    val currentUserId by sharedAuthViewModel.userId.collectAsState()

    LaunchedEffect(courseId) {
        Log.d("DiscussionForumScreen", "LaunchedEffect triggered for courseId: $courseId")
        currentDiscussionViewModel.loadCourseComments(courseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Discussions") },
                // No back button or other actions here as per previous request
            )
        },
        // Removed floatingActionButton and bottomBar (NavBar) as per previous request
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Discussion content area
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                if (topLevelComments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No discussion posts yet. Be the first to start one!", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f) // Makes the LazyColumn fill available space
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(items = topLevelComments, key = { comment -> comment.id }) { comment ->
                            // Pass currentUserId to CommentBubble
                            CommentBubble(comment = comment, currentUserId = currentUserId?.toString()) // Convert Int? to String?
                        }
                    }
                }
            }

            // Message input area at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newMessageContent,
                    onValueChange = { newMessageContent = it },
                    label = { Text("Write your message...") },
                    modifier = Modifier.weight(1f),
                    singleLine = false,
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )
                Button(
                    onClick = {
                        if (newMessageContent.isNotBlank()) {
                            currentDiscussionViewModel.postNewComment(courseId, newMessageContent)
                            newMessageContent = "" // Clear input field
                        }
                    },
                    enabled = newMessageContent.isNotBlank() // Enable button only if there's text
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Message")
                }
            }
        }
    }
}

@Composable
fun CommentBubble(comment: Comment, currentUserId: String?, level: Int = 0) {
    val isCurrentUser = comment.userId == currentUserId // Assuming userId in Comment is String
    val bubbleColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val authorTextColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary

    val indent = 16.dp * level // Indentation for replies

    // Use a Row to control horizontal alignment of the Card
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent, end = if (level == 0) 0.dp else 16.dp), // Indent replies
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start // Align the card within the Row
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 300.dp), // Limit max width, so it's not full width
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = comment.author.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = authorTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Recursively display replies, but ensure they are still within the main flow
    comment.replies?.forEach { reply ->
        Spacer(modifier = Modifier.height(4.dp)) // Small space between parent and child comment
        CommentBubble(comment = reply, currentUserId = currentUserId, level = level + 1)
    }
}