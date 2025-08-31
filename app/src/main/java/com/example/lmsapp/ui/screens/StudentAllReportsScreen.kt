package com.example.lmsapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
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
import com.example.lmsapp.ViewModels.StudentAllReportsViewModel
import com.example.lmsapp.ViewModels.SharedAuthViewModel

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAllReportsScreen(
    navController: NavController,
    studentAllReportsViewModel: StudentAllReportsViewModel,
    sharedAuthViewModel: SharedAuthViewModel,
    studentId: Int // Passed from route
) {
    val token by sharedAuthViewModel.token.collectAsState()
    val submissionSummaries by studentAllReportsViewModel.submissionSummaries.collectAsState()
    val isLoading by studentAllReportsViewModel.isLoading.collectAsState()
    val errorMessage by studentAllReportsViewModel.errorMessage.collectAsState()

    LaunchedEffect(studentId, token) {
        if (token.isNotEmpty() && studentId != 0) { // Ensure token and ID are valid
            studentAllReportsViewModel.fetchAllStudentReports(token, studentId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Past Quizzes") },
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
                Text("Loading your reports...")
            } else if (errorMessage != null) {
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            } else if (submissionSummaries.isEmpty()) {
                Text("You haven't completed any quizzes yet.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(submissionSummaries, key = { it.submissionId }) { summary ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    navController.navigate("studentreportsscreen/${summary.quizId}") {
                                        launchSingleTop = true
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = summary.quizTitle,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Score: ${summary.score} / ${summary.totalMarks} (${String.format("%.2f", summary.percentage)}%)",
                                    color = if (summary.percentage >= 50.0) Color.Green else Color.Red
                                )
                                Text(
                                    text = "Submitted: ${summary.submittedAt.substringBefore("T")}", // Basic date format
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}