package com.example.lmsapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lmsapp.ViewModels.ReportViewModel
import com.example.lmsapp.ViewModels.SharedAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentReportsScreen(
    navController: NavController,
    reportViewModel: ReportViewModel,
    quizId: Int,
    sharedAuthViewModel: SharedAuthViewModel,
    userId: Int
) {
    val quizReport by reportViewModel.quizReport.collectAsState()
    val isLoading by reportViewModel.isLoading.collectAsState()
    val errorMessage by reportViewModel.errorMessage.collectAsState()

    val token by sharedAuthViewModel.token.collectAsState()

    LaunchedEffect(quizId, userId, token) {
        reportViewModel.fetchQuizReport(token, quizId, userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
                Text("Loading report...")
            } else if (errorMessage != null) {
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            } else if (quizReport != null) {
                // Display the report summary
                Text(
                    text = "Quiz: ${quizReport?.quizTitle}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Student: ${quizReport?.studentUsername}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Score: ${quizReport?.score} / ${quizReport?.totalMarks} (${String.format("%.2f", quizReport?.percentage)}%)",
                    style = MaterialTheme.typography.titleLarge,
                    color = if ((quizReport?.percentage ?: 0.0) >= 50.0) Color.Green else Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Display individual question details
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quizReport?.questions ?: emptyList()) { question ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Q${question.questionId}: ${question.questionText}", style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(4.dp))
                                Text("Your Answer: ${question.studentSelectedOption ?: "N/A"}",
                                    color = if (question.isCorrect) Color.Green else Color.Red)
                                Text("Correct Answer: ${question.options.getOrNull(question.correctOptionIndex) ?: "N/A"}")
                                Text("Marks: ${question.mark}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            } else {
                Text("No report available.")
            }
        }
    }
}