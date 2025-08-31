package com.example.lmsapp.ui.screens

import CourseViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lmsapp.ui.data.DataClasses.StudentRegisterRequest
import com.example.lmsapp.ui.data.DataClasses.TeacherRegisterRequest
import com.example.lmsapp.ui.network.LMSRepository

import com.example.lmsapp.ViewModels.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    token: String,
    onBackToLogin: () -> Unit // Callback for "Back to Login"
) {
    // Reset the ViewModel's state when this screen is first launched
    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    // Role state for radio buttons
    var role by remember { mutableStateOf("student") } // Default to "student"
    val roles = listOf("student", "teacher")

    // Common fields
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    // Student-specific
    var enrollmentNumber by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var batchYear by remember { mutableStateOf("") }

    // Teacher-specific
    var employeeId by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var yearsOfExperience by remember { mutableStateOf("") }
    var qualifications by remember { mutableStateOf("") }

    val registerStatus = viewModel.registerStatus
    val isLoading = viewModel.isLoading

    val scrollState = rememberScrollState() // Create a ScrollState
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModel.CourseViewModelFactory(
            LMSRepository(),
            savedStateRegistryOwner
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 24.dp) // Add horizontal padding
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Use spacedBy for consistent spacing
        ) {
            // --- Role Selection with Radio Buttons ---
            Text("Select Role:", style = MaterialTheme.typography.titleMedium)
            Row(
                Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                roles.forEach { text ->
                    Row(
                        Modifier
                            .weight(1f) // Distribute space evenly
                            .height(50.dp)
                            .selectable(
                                selected = (text == role),
                                onClick = { role = text },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == role),
                            onClick = null // null recommended for accessibility with selectable
                        )
                        Text(
                            text = text.replaceFirstChar { it.uppercase() }, // Capitalize first letter for display
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            // --- End Role Selection ---

            Spacer(modifier = Modifier.height(4.dp)) // Add some space below role selection

            // Common fields
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // More specific keyboard type
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())

            if (role == "student") {
                OutlinedTextField(
                    value = enrollmentNumber,
                    onValueChange = { enrollmentNumber = it },
                    label = { Text("Enrollment Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Assuming it's numeric or alphanumeric
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(value = semester, onValueChange = { semester = it }, label = { Text("Semester") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = batchYear, onValueChange = { batchYear = it }, label = { Text("Batch Year") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }

            if (role == "teacher") {
                OutlinedTextField(value = employeeId, onValueChange = { employeeId = it }, label = { Text("Employee ID (Optional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("Designation") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = yearsOfExperience, onValueChange = { yearsOfExperience = it }, label = { Text("Years of Experience") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qualifications, onValueChange = { qualifications = it }, label = { Text("Qualifications") }, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register button
            Button(
                onClick = {
                    if (role == "student") {
                        val request = StudentRegisterRequest(
                            username = username,
                            password = password,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            phoneNumber = phoneNumber,
                            role = "student",
                            enrollmentNumber = enrollmentNumber,
                            department = "CSE", // You might want to make this selectable too
                            semester = semester.toIntOrNull() ?: 0,
                            batchYear = batchYear.toIntOrNull() ?: 0
                        )
                        viewModel.registerStudent(request)
                    } else if (role == "teacher") {
                        val request = TeacherRegisterRequest(
                            username = username,
                            password = password,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            phoneNumber = phoneNumber,
                            role = "teacher",
                            employeeId = (if (employeeId.isNotEmpty()) employeeId else null).toString(),
                            department = "CSE", // You might want to make this selectable too
                            designation = designation,
                            yearsOfExperience = yearsOfExperience.toIntOrNull() ?: 0,
                            qualifications = qualifications
                        )
                        viewModel.registerTeacher(request)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading // Disable button while registering
            ) {
                Text(if (isLoading) "Registering..." else "Register")
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = registerStatus)

            LaunchedEffect(registerStatus) {
                if (registerStatus == "Student registration successful" || registerStatus == "Teacher registration successful") {
                    onRegisterSuccess()
                    courseViewModel.fetchStudents(token)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    val mockViewModel = RegisterViewModel(LMSRepository())
    RegisterScreen(viewModel = mockViewModel, onRegisterSuccess = {}, onBackToLogin = {}, token = "sample_token")
}