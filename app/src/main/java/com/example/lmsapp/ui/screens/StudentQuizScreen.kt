package com.example.lmsapp.ui.screens

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize // Added fillMaxSize import
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator // Added CircularProgressIndicator import
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect // Added DisposableEffect import
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lmsapp.ui.network.LMSRepository
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import com.example.lmsapp.ViewModels.StudentQuizViewModel
import com.example.lmsapp.ViewModels.StudentQuizViewModel.StudentQuizViewModelFactory
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentQuizScreen(
    token: String,
    quizId: Int,
    navController: NavController,
    sharedAuthViewModel: SharedAuthViewModel,
    viewModel: StudentQuizViewModel = viewModel(
        factory = StudentQuizViewModelFactory(LMSRepository(), sharedAuthViewModel)
    )
) {
    val questions by viewModel.questions.collectAsState()
    val studentAnswers by viewModel.studentAnswers.collectAsState()
    val score by viewModel.score.collectAsState()
    val totalMarks by viewModel.totalMarks.collectAsState()
    val quizTimeLimitInMinutes by viewModel.timeLimit.collectAsState()
    val submissionStatus by viewModel.submissionStatus.collectAsState()

    // State for the timer
    var timeLeft by remember { mutableLongStateOf(0L) }
    var quizSubmitted by remember { mutableStateOf(false) }
    var timer: CountDownTimer? by remember { mutableStateOf(null) } // Hold a reference to the timer
    var quizSubmittedManually by remember { mutableStateOf(false) }

    // Fetch quiz details when screen is launched
    LaunchedEffect(Unit) {
        viewModel.fetchQuizById(token, quizId)
    }

    LaunchedEffect(quizTimeLimitInMinutes, questions) {
        if (quizTimeLimitInMinutes > 0 && questions.isNotEmpty() && !quizSubmitted) {
            val initialTimeMillis = quizTimeLimitInMinutes * 60 * 1000L
            timeLeft = initialTimeMillis

            timer?.cancel()

            timer = object : CountDownTimer(initialTimeMillis, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = millisUntilFinished
                }

                override fun onFinish() {
                    if (!quizSubmitted) {
                        viewModel.submitQuiz(token, quizId)
                    }
                }
            }
            timer?.start()
        }
    }

    LaunchedEffect(submissionStatus) {
        when (submissionStatus) {
            StudentQuizViewModel.SubmissionStatus.SUCCESS -> {
                Log.d("StudentQuizScreen", "Quiz submitted successfully. Navigating to reports.")
                timer?.cancel() // Ensure timer is stopped
                navController.popBackStack() // Go back from quiz screen
                navController.navigate("studentreportsscreen/$quizId") // Navigate to reports, passing quizId
                viewModel.resetSubmissionStatus() // Reset status after navigation
            }
            StudentQuizViewModel.SubmissionStatus.FAILED -> {
                Log.e("StudentQuizScreen", "Quiz submission failed. Displaying error.")
                // Optionally, show a SnackBar or Toast to the user about the failure
                viewModel.resetSubmissionStatus() // Reset status after handling
            }
            else -> { /* Do nothing for IDLE or IN_PROGRESS */ }
        }
    }

    // Cleanup the timer when the composable leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            timer?.cancel() // Cancel the timer when the composable is disposed
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Quiz")
                        Spacer(modifier = Modifier.weight(1f)) // Push timer to the right
                        Text(formatTime(timeLeft)) // Display timer
                    }
                },
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
                .padding(horizontal = 16.dp)
                .fillMaxSize() // Ensure Column fills the screen to center loading
        ) {
            Spacer(Modifier.height(8.dp))

            when {
                questions.isEmpty() && submissionStatus == StudentQuizViewModel.SubmissionStatus.IDLE -> {
                    // Show loading indicator if questions are not yet loaded
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    Text("Loading quiz...")
                }
                submissionStatus == StudentQuizViewModel.SubmissionStatus.IN_PROGRESS -> {
                    // Show loading indicator when submitting
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    Text("Submitting quiz...")
                }
                quizSubmitted -> {
                    // Display results after submission (or just a message before navigation)
                    Text("Quiz Submitted!", style = MaterialTheme.typography.titleLarge)
                    Text("Your Score: $score / $totalMarks", style = MaterialTheme.typography.titleMedium)
                }
                else -> {
                    // Display questions if loaded and not submitted
                    questions.forEach { question ->
                        Log.d("QuizDebug", "Rendering Question ID: ${question.id}, Text: ${question.questionText}, Options: ${question.options}")
                        Text("Q${question.id}: ${question.questionText}")
                        question.options.forEach { option ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = studentAnswers[question.id] == option,
                                    onClick = { viewModel.setAnswer(question.id, option) }
                                )
                                Text(option)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            // Cancel timer immediately when button is clicked
                            timer?.cancel()
                            quizSubmittedManually = true // Mark as manually submitted
                            viewModel.submitQuiz(token, quizId)
                        },
                        // Disable button if already submitting or if submitted manually
                        enabled = submissionStatus == StudentQuizViewModel.SubmissionStatus.IDLE && !quizSubmittedManually
                    ) {
                        Text("Submit Quiz")
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}