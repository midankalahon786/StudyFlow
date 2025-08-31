package com.example.lmsapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lmsapp.ui.data.Question
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import com.example.lmsapp.ViewModels.TeacherQuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizScreen(
    quizViewModel: TeacherQuizViewModel,
    navController: NavController,
    authViewModel: SharedAuthViewModel,
    username: String
) {

    val token by authViewModel.token.collectAsState()
    val userId by authViewModel.userId.collectAsState() // Collect the userId
    Log.d("CreateQuizScreen", "userId: $userId")
    val previousQuizzes by quizViewModel.quizList.collectAsState()
    val isLoading by quizViewModel.isLoading.collectAsState()
    val role by authViewModel.role.collectAsState() //not used
    // Local state for text input fields
    var timeLimit by rememberSaveable { mutableStateOf("") }
    var negativeMarking by rememberSaveable { mutableStateOf("") }
    var totalMarks by rememberSaveable { mutableStateOf("") }
    var showAddQuestionDialog by rememberSaveable { mutableStateOf(false) }

    // Collecting state from the ViewModel (state flows)
    val questions by quizViewModel.questions.collectAsState()
    val quizTitle by quizViewModel.quizTitle.collectAsState()
    val quizCreationSuccess by quizViewModel.quizCreationSuccess.collectAsState(initial = null)

    val snackBarHostState = remember { SnackbarHostState() }

    // Basic validation: ensure quiz title, timeLimit, totalMarks, and negativeMarking are valid
    val isInputValid = quizTitle.isNotBlank() &&
            timeLimit.toIntOrNull() != null &&
            totalMarks.toIntOrNull() != null &&
            negativeMarking.toFloatOrNull() != null

    // LaunchedEffect to react to quiz creation success
    LaunchedEffect(quizCreationSuccess) {
        if (quizCreationSuccess == true) {
            snackBarHostState.showSnackbar("Quiz created successfully!")
            timeLimit = ""
            totalMarks = ""
            negativeMarking = ""
            quizViewModel.quizTitle.value = ""
        } else if (quizCreationSuccess == false) {
            snackBarHostState.showSnackbar("Failed to create quiz.")
        }
        // Reset the success state to null to observe future creations
        quizViewModel.resetQuizCreationSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Back to Quizzes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        // Use a single scrollable LazyColumn for the entire screen content.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quiz Title Input
            item {
                TextField(
                    value = quizTitle,
                    onValueChange = { quizViewModel.quizTitle.value = it },
                    label = { Text("Quiz Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Time Limit Input
            item {
                TextField(
                    value = timeLimit,
                    onValueChange = { timeLimit = it },
                    label = { Text("Time Limit (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Total Marks Input
            item {
                TextField(
                    value = totalMarks,
                    onValueChange = { totalMarks = it },
                    label = { Text("Total Marks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Negative Marking Input
            item {
                TextField(
                    value = negativeMarking,
                    onValueChange = { negativeMarking = it },
                    label = { Text("Negative Marking (e.g., -0.25)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Button to open the "Add Question" dialog
            item {
                Button(
                    onClick = { showAddQuestionDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Question")
                }
            }
            // Questions header
            item {
                Text("Questions:", style = MaterialTheme.typography.titleMedium)
            }
            // Questions list; if empty, show a friendly message
            if (questions.isNotEmpty()) {
                items(questions) { question ->
                    QuestionItem(question)
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            "No questions added yet.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            // "Create Quiz" button with validation; clears inputs upon successful submission.
            item {
                Button(
                    onClick = {
                        quizViewModel.timeLimit.value = timeLimit.toIntOrNull() ?: 0
                        quizViewModel.totalMarks.value = totalMarks.toIntOrNull() ?: 0
                        quizViewModel.negativeMarking.value = negativeMarking.toFloatOrNull() ?: 0.0f

                        // Pass the token AND userId here
                        if (userId != null) {
                            quizViewModel.createQuiz(token)
                        }
                        // Do NOT clear fields here anymore
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isInputValid && !isLoading && userId != null // Disable button while loading
                ) {
                    Text(if (isLoading) "Creating Quiz..." else "Create Quiz")
                }
            }
        }
    }

    // "Add Question" dialog
    if (showAddQuestionDialog) {
        AddQuestionDialog(
            onDismissRequest = { showAddQuestionDialog = false },
            onAddQuestion = { questionText, optionA, optionB, optionC, optionD, correctAnswer, questionMarks ->
                // Validate dialog inputs before adding the question
                if (questionText.isNotBlank() &&
                    optionA.isNotBlank() &&
                    optionB.isNotBlank() &&
                    optionC.isNotBlank() &&
                    optionD.isNotBlank() &&
                    (correctAnswer in listOf("A", "B", "C", "D")) &&
                    questionMarks.toIntOrNull() != null
                ) {
                    quizViewModel.addQuestion(
                        questionText,
                        listOf(optionA, optionB, optionC, optionD),
                        correctAnswer,
                        questionMarks.toIntOrNull() ?: 0
                    )
                }
                showAddQuestionDialog = false
            }
        )
    }
}

@Composable
fun QuestionItem(question: Question) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = "Question: ${question.questionText}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Option A: ${question.options[0]}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Option B: ${question.options[1]}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Option C: ${question.options[2]}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Option D: ${question.options[3]}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Correct Answer: ${question.correctOptionIndex}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Marks: ${question.mark}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionDialog(
    onDismissRequest: () -> Unit,
    onAddQuestion: (String, String, String, String, String, String, String) -> Unit
) {
    var questionText by rememberSaveable { mutableStateOf("") }
    var optionA by rememberSaveable { mutableStateOf("") }
    var optionB by rememberSaveable { mutableStateOf("") }
    var optionC by rememberSaveable { mutableStateOf("") }
    var optionD by rememberSaveable { mutableStateOf("") }
    var correctAnswer by rememberSaveable { mutableStateOf("") }
    var questionMarks by rememberSaveable { mutableStateOf("") }

    // For a dropdown menu for the correct answer
    val answerOptions = listOf("A", "B", "C", "D")
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Determine if all fields are valid
    val isDialogInputValid = questionText.isNotBlank() &&
            optionA.isNotBlank() &&
            optionB.isNotBlank() &&
            optionC.isNotBlank() &&
            optionD.isNotBlank() &&
            (correctAnswer in answerOptions) &&
            questionMarks.toIntOrNull() != null

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Add Question") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = optionA,
                    onValueChange = { optionA = it },
                    label = { Text("Option A") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = optionB,
                    onValueChange = { optionB = it },
                    label = { Text("Option B") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = optionC,
                    onValueChange = { optionC = it },
                    label = { Text("Option C") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = optionD,
                    onValueChange = { optionD = it },
                    label = { Text("Option D") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Dropdown for Correct Answer
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    TextField(
                        value = correctAnswer,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Correct Answer") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        answerOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    correctAnswer = option
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                TextField(
                    value = questionMarks,
                    onValueChange = { questionMarks = it },
                    label = { Text("Marks for this Question") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddQuestion(
                        questionText,
                        optionA,
                        optionB,
                        optionC,
                        optionD,
                        correctAnswer,
                        questionMarks
                    )
                },
                enabled = isDialogInputValid
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
