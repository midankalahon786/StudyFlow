package com.example.lmsapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lmsapp.ViewModels.ReportViewModel
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import android.util.Log


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    reportViewModel: ReportViewModel,
    sharedAuthViewModel: SharedAuthViewModel,
    quizId: Int,
    userId: Int
) {
    val quizReport by reportViewModel.quizReport.collectAsState()
    val isLoading by reportViewModel.isLoading.collectAsState()
    val errorMessage by reportViewModel.errorMessage.collectAsState()

    val token by sharedAuthViewModel.token.collectAsState()

    LaunchedEffect(quizId, userId, token) {
            reportViewModel.clearReport()
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
                // Show loading indicator
                CircularProgressIndicator()
                Text("Loading report...", modifier = Modifier.padding(top = 8.dp))
            } else if (errorMessage != null) {
                // Show error message
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (quizReport != null) {
                val report = quizReport!! // Assert non-null here as it's checked above

                // Display the report summary
                Text(
                    text = "Quiz: ${report.quizTitle}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Log.d("ReportScreen","Quiz Title: ${report.quizTitle}")
                Text(
                    text = "Student: ${report.studentUsername}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Log.d("ReportScreen","Username: ${report.studentUsername}")
                Text(
                    text = "Score: ${report.score} / ${report.totalMarks} (${String.format("%.2f", report.percentage)}%)",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (report.percentage >= 50.0) Color.Green else Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Display individual question details
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(report.questions.orEmpty()) { question ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Q${question.questionId}: ${question.questionText}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Your Answer: ${question.studentSelectedOption ?: "Not answered"}",
                                    color = if (question.isCorrect) Color.Green else Color.Red
                                )

                                Text(
                                    text = "Correct Answer: ${question.options.getOrNull(question.correctOptionIndex) ?: "N/A"}",
                                    color = Color.Blue
                                )
                                Text("Marks: ${question.mark}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    // Optional: Show a message if there are no questions to display
                    if (report.questions.isEmpty() && !isLoading) {
                        item {
                            Text("No question details found for this report.",
                                modifier = Modifier.padding(top = 16.dp))
                        }
                    }
                }
            } else {
                // If report is null and not loading/error, show a default message
                Text("No report available for this quiz.", modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}