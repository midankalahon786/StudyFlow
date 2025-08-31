package com.example.lmsapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lmsapp.ui.data.DataClasses.Quiz
import com.example.lmsapp.ui.screens.HomeScreenUiElements.NavBar
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import com.example.lmsapp.ViewModels.TeacherQuizViewModel
import android.util.Log // For logging, useful for delete/edit actions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCreateQuizScreen(
    navController: NavController,
    teacherQuizViewModel: TeacherQuizViewModel,
    sharedAuthViewModel: SharedAuthViewModel
) {
    val token by sharedAuthViewModel.token.collectAsState()
    val previousQuizzes by teacherQuizViewModel.quizList.collectAsState()
    val isLoading by teacherQuizViewModel.isLoading.collectAsState()
    val role by sharedAuthViewModel.role.collectAsState()
    val username by sharedAuthViewModel.username.collectAsState()
    val deletionStatus by teacherQuizViewModel.deletionStatus.collectAsState()

    // Observe deletion status to potentially refresh the list or show messages
    LaunchedEffect(deletionStatus) {
        when (deletionStatus) {
            TeacherQuizViewModel.DeletionStatus.SUCCESS -> {
                Log.d("TeacherCreateQuizScreen", "Quiz deleted successfully. Refreshing list.")
                teacherQuizViewModel.fetchQuizzes(token) // Refresh the list after successful deletion
                teacherQuizViewModel.resetDeletionStatus() // Reset status
            }
            TeacherQuizViewModel.DeletionStatus.FAILED -> {
                Log.e("TeacherCreateQuizScreen", "Failed to delete quiz.")
                // Optionally show a SnackBar or Toast to the user
                teacherQuizViewModel.resetDeletionStatus() // Reset status
            }
            else -> {} // Do nothing for IDLE or IN_PROGRESS
        }
    }

    LaunchedEffect(token) {
        if (token.isNotBlank()) { // Important: Only fetch if token is available
            teacherQuizViewModel.fetchQuizzes(token)
        }
    }

    Scaffold(
        bottomBar = {
            NavBar(
                navController = navController,
                role = role,
                username = username
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize() // Use fillMaxSize for column containing quizzes
        ) {
            Button(
                onClick = {
                    // Navigate to a screen where the teacher can input new quiz details.
                    // This could be "addQuizScreen" or "createQuizScreen"
                    navController.navigate("upload_question_bank") // Assuming you'll create this route
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create a New Quiz")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Previous Quizzes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(text = "Loading quizzes...", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (previousQuizzes.isEmpty()) {
                Text(text = "No quizzes created yet.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // Use LazyColumn for efficient scrolling if there are many quizzes
                // For now, keeping forEach for simplicity, but consider LazyColumn for performance.
                previousQuizzes.forEach { quiz ->
                    QuizCard(
                        quiz = quiz,
                        onViewReport = {
                            navController.navigate("quizDetails/${quiz.id}") // Navigate to quiz details/report
                        },
                        onEditQuiz = {
                            navController.navigate("editQuizScreen/${quiz.id}")
                        },
                        onDeleteQuiz = {

                            Log.d("TeacherCreateQuizScreen", "Attempting to delete quiz ID: ${quiz.id}")
                            teacherQuizViewModel.deleteQuiz(token, quiz.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun QuizCard(
    quiz: Quiz,
    onViewReport: () -> Unit,
    onEditQuiz: () -> Unit,    // New edit callback
    onDeleteQuiz: () -> Unit   // New delete callback
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Reduced vertical padding here for better spacing in list
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Title: ${quiz.title}", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Time Limit: ${quiz.timeLimit} minutes", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Total Marks: ${quiz.totalMarks}", style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween, // Distribute buttons horizontally
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onViewReport,
                    modifier = Modifier.weight(1f) // Take up available space
                ) {
                    Text("View Details")
                }

                Spacer(modifier = Modifier.width(8.dp)) // Spacer between buttons

                IconButton(
                    onClick = onEditQuiz,
                    modifier = Modifier
                        .size(48.dp) // Standard touch target size
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Quiz",
                        tint = MaterialTheme.colorScheme.primary // Use primary color for edit icon
                    )
                }

                IconButton(
                    onClick = onDeleteQuiz,
                    modifier = Modifier
                        .size(48.dp) // Standard touch target size
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Quiz",
                        tint = MaterialTheme.colorScheme.error // Use error color for delete icon
                    )
                }
            }
        }
    }
}