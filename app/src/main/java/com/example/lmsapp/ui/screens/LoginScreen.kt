package com.example.lmsapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lmsapp.R
import com.example.lmsapp.ViewModels.LoginViewModel
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import com.example.lmsapp.ui.network.LMSRepository
import com.example.lmsapp.ui.theme.Verdigris

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    sharedAuthViewModel: SharedAuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToNext: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val errorMessage = viewModel.errorMessage

    // Make sure to observe the ViewModel's token and role states
    val token = viewModel.token
    val role = viewModel.userRole
    val userId = sharedAuthViewModel.userId.value
    val isLoading = viewModel.isLoading // Track loading state

    Column(
        modifier = Modifier
            .padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.studyflow),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "StudyFlow",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.size(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.LightGray,
                focusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Blue,
                unfocusedIndicatorColor = Color.Gray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.DarkGray
            )
        )
        Spacer(modifier = Modifier.size(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val imageVector = if (passwordVisible)
                    ImageVector.vectorResource(id = R.drawable.baseline_visibility_24)
                else
                    ImageVector.vectorResource(id = R.drawable.baseline_visibility_off_24)

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = imageVector, contentDescription = description)
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.LightGray,
                focusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Blue,
                unfocusedIndicatorColor = Color.Gray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.DarkGray
            )
        )
        Spacer(modifier = Modifier.size(16.dp))
        Button(onClick = {
            loginMessage = "Logging in..."
            viewModel.login(username, password) // Initiate the login in the ViewModel
        },
            colors = ButtonDefaults.buttonColors(
                containerColor = Verdigris,
                contentColor = Color.White
            )) {
            Text("Login", fontWeight = FontWeight.Bold)
        }

        // Display login success or failure message
        if (isLoading) {
            loginMessage = "Logging in..."
        } else if (!errorMessage.isNullOrBlank()) {
            loginMessage = errorMessage
            Log.e("LoginScreen", "Login error: $loginMessage")
        } else if (token != null && role != null) {
            loginMessage = "Login successful!"
            sharedAuthViewModel.setAuthData(token, role, username, userId)
            onNavigateToNext() // Navigate after setting shared auth data
        }

        Text(text = loginMessage)

        Text("Not Registered?", fontWeight = FontWeight.Bold)
        Button(onClick = onNavigateToRegister, colors = ButtonDefaults.buttonColors(
            containerColor = Verdigris,
            contentColor = Color.White
        )) {
            Text("Register Now", fontWeight =  FontWeight.Bold)
        }
        Spacer(modifier = Modifier.size(48.dp))

// Add the copyright notice at the bottom of the column.
        Text(
            text = "© 2025 StudyFlow. All rights reserved.",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview(){
    // Create a mock context for the preview
    val mockContext = LocalContext.current

    // Instantiate LMSRepository with the mock context
    val fakeRepository = remember {
        LMSRepository()
    }

    // Create a dummy SharedAuthViewModel
    val fakeSharedAuthViewModel = remember { SharedAuthViewModel(mockContext) }

    // Instantiate the LoginViewModel with the fake dependencies
    val fakeViewModel = remember {
        LoginViewModel(
            repository = fakeRepository,
            sharedAuthViewModel = fakeSharedAuthViewModel
        )
    }

    // Now call the LoginScreen with the instantiated ViewModels
    LoginScreen(
        viewModel = fakeViewModel,
        sharedAuthViewModel = fakeSharedAuthViewModel,
        onNavigateToRegister = {},
        onNavigateToNext = {}
    )
}
