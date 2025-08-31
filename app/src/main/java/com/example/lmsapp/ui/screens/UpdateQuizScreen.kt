package com.example.lmsapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lmsapp.ui.data.Question
import com.example.lmsapp.ui.data.DataClasses.Quiz
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import com.example.lmsapp.ViewModels.TeacherQuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateQuizScreen(
    navController: NavController,
    quizId: Int,
    teacherQuizViewModel: TeacherQuizViewModel,
    sharedAuthViewModel: SharedAuthViewModel
) {
    val token by sharedAuthViewModel.token.collectAsState()

    // ViewModel states for quiz details
    val quizTitle by teacherQuizViewModel.quizTitle.collectAsState()
    val timeLimit by teacherQuizViewModel.timeLimit.collectAsState()
    val negativeMarking by teacherQuizViewModel.negativeMarking.collectAsState()
    val totalMarks by teacherQuizViewModel.totalMarks.collectAsState()
    val questions by teacherQuizViewModel.questions.collectAsState()
    val isLoading by teacherQuizViewModel.isLoading.collectAsState()
    val quizUpdateSuccess by teacherQuizViewModel.quizUpdateSuccess.collectAsState()

    // State for showing "Add Question" dialog
    var showAddQuestionDialog by remember { mutableStateOf(false) }

    // LaunchedEffect to load quiz data when screen is launched
    LaunchedEffect(quizId, token) {
        if (token.isNotBlank()) {
            Log.d("UpdateQuizScreen", "Fetching quiz for update: $quizId")
            teacherQuizViewModel.loadQuizForEdit(token, quizId)
        }
    }

    // LaunchedEffect for handling quiz update success/failure
    LaunchedEffect(quizUpdateSuccess) {
        when (quizUpdateSuccess) {
            true -> {
                Log.d("UpdateQuizScreen", "Quiz updated successfully, navigating back.")
                navController.popBackStack() // Go back to the previous screen (TeacherCreateQuizScreen)
                teacherQuizViewModel.resetQuizUpdateSuccess() // Reset the state
            }
            false -> {
                Log.e("UpdateQuizScreen", "Failed to update quiz.")
                // Optionally show a SnackBar or Toast to the user about the failure
                teacherQuizViewModel.resetQuizUpdateSuccess() // Reset the state
            }
            null -> { /* Do nothing */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Quiz") },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                Text("Loading quiz details...")
            } else {
                OutlinedTextField(
                    value = quizTitle,
                    onValueChange = { teacherQuizViewModel.quizTitle.value = it },
                    label = { Text("Quiz Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = timeLimit.toString(),
                    onValueChange = { teacherQuizViewModel.timeLimit.value = it.toIntOrNull() ?: 0 },
                    label = { Text("Time Limit (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = negativeMarking.toString(),
                    onValueChange = { teacherQuizViewModel.negativeMarking.value = it.toFloatOrNull() ?: 0.0f },
                    label = { Text("Negative Marking (e.g., 0.25)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                // Total marks can be calculated or manually set. For now, assuming manual.
                OutlinedTextField(
                    value = totalMarks.toString(),
                    onValueChange = { teacherQuizViewModel.totalMarks.value = it.toIntOrNull() ?: 0 },
                    label = { Text("Total Marks") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // --- Questions Section ---
                Text("Questions", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (questions.isEmpty()) {
                    Text("No questions added yet.")
                }

                LazyColumn(
                    modifier = Modifier.weight(1f) // Allow questions to take up available height
                ) {
                    itemsIndexed(questions) { index, question ->
                        QuestionEditCard(
                            question = question,
                            onUpdate = { updatedQuestion ->
                                teacherQuizViewModel.updateQuestionInList(updatedQuestion)
                            },
                            onDelete = {
                                teacherQuizViewModel.removeQuestionFromList(question)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))


                Button(
                    onClick = { showAddQuestionDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Question")
                    Spacer(Modifier.width(4.dp))
                    Text("Add New Question")
                }
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Create a Quiz object from current ViewModel states
                        val updatedQuiz = Quiz(
                            id = quizId, // Crucial: Pass the existing quiz ID
                            title = quizTitle,
                            timeLimit = timeLimit,
                            negativeMarking = negativeMarking,
                            totalMarks = totalMarks,
                            questions = questions,
                        )
                        teacherQuizViewModel.updateQuiz(token, quizId, updatedQuiz)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading // Disable button while loading/updating
                ) {
                    Text("Update Quiz")
                }
            }
        }
    }

    // Dialog for adding a new question
    if (showAddQuestionDialog) {
        AddQuestionDialog(
            onDismiss = { showAddQuestionDialog = false },
            onAddQuestion = { qText, options, correctAns, marks ->
                teacherQuizViewModel.addQuestion(qText, options, correctAns, marks)
                showAddQuestionDialog = false
            }
        )
    }
}

// --- Composable for editing individual questions ---
@Composable
fun QuestionEditCard(
    question: Question,
    onUpdate: (Question) -> Unit,
    onDelete: () -> Unit
) {
    var questionText by remember { mutableStateOf(question.questionText) }
    var optionA by remember { mutableStateOf(question.options.getOrElse(0) { "" }) }
    var optionB by remember { mutableStateOf(question.options.getOrElse(1) { "" }) }
    var optionC by remember { mutableStateOf(question.options.getOrElse(2) { "" }) }
    var optionD by remember { mutableStateOf(question.options.getOrElse(3) { "" }) }
    var correctOption by remember { mutableStateOf(
        when (question.correctOptionIndex) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            else -> ""
        }
    ) }
    var mark by remember { mutableIntStateOf(question.mark) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Question ID: ${question.id}", style = MaterialTheme.typography.labelSmall)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Question", tint = Color.Red)
                }
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = questionText,
                onValueChange = {
                    questionText = it
                    onUpdate(question.copy(questionText = it))
                },
                label = { Text("Question Text") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Options (A, B, C, D)
            val options = remember(optionA, optionB, optionC, optionD) {
                listOf(optionA, optionB, optionC, optionD)
            }

            OutlinedTextField(value = optionA, onValueChange = {
                optionA = it; onUpdate(question.copy(options = options.toMutableList().apply { this[0] = it }))
            }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = optionB, onValueChange = {
                optionB = it; onUpdate(question.copy(options = options.toMutableList().apply { this[1] = it }))
            }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = optionC, onValueChange = {
                optionC = it; onUpdate(question.copy(options = options.toMutableList().apply { this[2] = it }))
            }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = optionD, onValueChange = {
                optionD = it; onUpdate(question.copy(options = options.toMutableList().apply { this[3] = it }))
            }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = correctOption,
                onValueChange = {
                    correctOption = it
                    val newIndex = when (it.uppercase()) {
                        "A" -> 0
                        "B" -> 1
                        "C" -> 2
                        "D" -> 3
                        else -> -1 // Invalid option
                    }
                    onUpdate(question.copy(correctOptionIndex = newIndex))
                },
                label = { Text("Correct Option (A, B, C, D)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = mark.toString(),
                onValueChange = {
                    mark = it.toIntOrNull() ?: 0
                    onUpdate(question.copy(mark = mark))
                },
                label = { Text("Marks for Question") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- Composable for adding a new question (dialog) ---
@Composable
fun AddQuestionDialog(
    onDismiss: () -> Unit,
    onAddQuestion: (questionText: String, options: List<String>, correctAnswer: String, marks: Int) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctOption by remember { mutableStateOf("") } // A, B, C, or D
    var marks by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Question") },
        text = {
            Column {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = optionA, onValueChange = { optionA = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = optionB, onValueChange = { optionB = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = optionC, onValueChange = { optionC = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = optionD, onValueChange = { optionD = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = correctOption,
                    onValueChange = { correctOption = it },
                    label = { Text("Correct Option (A, B, C, D)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = marks,
                    onValueChange = { marks = it },
                    label = { Text("Marks") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val options = listOf(optionA, optionB, optionC, optionD)
                    onAddQuestion(questionText, options, correctOption, marks.toIntOrNull() ?: 0)
                }
            ) {
                Text("Add Question")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}