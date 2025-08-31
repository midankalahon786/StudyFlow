package com.example.lmsapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log // Import Log for debugging
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lmsapp.ui.screens.HomeScreenUiElements.NavBar
import com.example.lmsapp.ViewModels.StudentQuizListViewModel
import com.example.lmsapp.ViewModels.SharedAuthViewModel // Import SharedAuthViewModel

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentQuizListScreen(
    navController: NavController,
    token: String,
    username: String,
    viewModel: StudentQuizListViewModel,
    sharedAuthViewModel: SharedAuthViewModel // Pass SharedAuthViewModel here
) {
    // Collect the quizzes list and loading state from the ViewModel
    val quizzes = viewModel.quizList.collectAsState(initial = emptyList()).value
    val isLoading = viewModel.isLoading.collectAsState(initial = false).value

    // Get the userId from SharedAuthViewModel
    val userId = sharedAuthViewModel.userId.collectAsState().value

    // Fetch quizzes when the screen is first launched
    LaunchedEffect(Unit) {
        viewModel.fetchQuizzes(token)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Available Quizzes") }) },
        bottomBar = { NavBar(navController, role = "student", username = username) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(), // Ensure the column takes full width for horizontal centering
            horizontalAlignment = Alignment.CenterHorizontally // Center the loading indicator
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                Text("Loading quizzes...")
            } else if (quizzes.isEmpty()) {
                Text("No quizzes available at the moment.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) { // Use weight to allow the button below
                    items(quizzes, key = { it.id }) { quiz ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable {
                                    navController.navigate("quizDetails/${quiz.id}")
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = quiz.title, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Marks: ${quiz.totalMarks}")
                                Text(text = "Time: ${quiz.timeLimit} mins")
                                // Note: 'createdBy' refers to userId of the teacher.
                                // If you want to show teacher's username, you'll need another API call or association.
                                Text(text = "By: ${quiz.createdBy}")
                            }
                        }
                    }
                }
            }

            // --- Section for Previous Quiz Reports ---
            Spacer(modifier = Modifier.height(16.dp)) // Add some space

            // Conditionally show button if userId is available
            if (userId != null) {
                Button(
                    onClick = {
                        Log.d("StudentQuizListScreen", "Navigating to all student reports for userId: $userId")
                        navController.navigate("studentallreports/${userId}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("View Past Quiz Reports")
                }
            } else {
                Log.w("StudentQuizListScreen", "User ID is null, cannot show 'View Past Quiz Reports' button.")
                Text("Login to view your past reports.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacer at the bottom
        }
    }
}