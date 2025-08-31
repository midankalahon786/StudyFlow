package com.example.lmsapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import com.example.lmsapp.ViewModels.StudentQuizDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailsScreen(
    quizId: Int?,
    userRole: String, // Add userRole parameter
    viewModel: StudentQuizDetailsViewModel,
    sharedAuthViewModel: SharedAuthViewModel,
    navController: NavController
) {
    val token by sharedAuthViewModel.token.collectAsState()
    // Fetch quiz details when screen is launched
    LaunchedEffect(quizId) {
        Log.d("QuizDetailsScreen", "Fetching Token and QuizId: $token, $quizId")
        viewModel.fetchQuizDetails(token, quizId)
    }

    // Collect quiz details and loading state from the ViewModel
    val quizDetails = viewModel.quizDetails.collectAsState(initial = null).value
    val isLoading = viewModel.isLoading.collectAsState(initial = false).value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                quizDetails?.let { quiz ->
                    Card(modifier = Modifier.padding(8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Quiz ID: ${quiz.id}", style = MaterialTheme.typography.bodyMedium) // Using quiz.id from fetched details
                            Text(text = "Title: ${quiz.title}", style = MaterialTheme.typography.titleLarge)
                            Text(text = "Total Marks: ${quiz.totalMarks}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Time Limit: ${quiz.timeLimit} mins", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))


                            Log.d("QuizDetailsScreen", "UserRole: $userRole")
                            // --- Take Quiz Button for Students ---
                            if (userRole == "student") { // Assuming "student" is the role string
                                Button(
                                    onClick = {
                                        quiz.id.let {
                                            // Navigate to StudentQuizScreen, passing the quiz ID
                                            navController.navigate("studentQuiz/${it}")
                                        }
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Take Quiz")
                                }
                            }
                            // --- End Take Quiz Button ---
                        }
                    }
                } ?: run {
                    Text("No quiz details available", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}