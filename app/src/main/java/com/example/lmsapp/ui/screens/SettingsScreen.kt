package com.example.lmsapp.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lmsapp.R
import com.example.lmsapp.ui.screens.HomeScreenUiElements.NavBar
import com.example.lmsapp.ViewModels.SettingsViewModel
import com.example.lmsapp.ViewModels.SharedAuthViewModel // Import SharedAuthViewModel
import androidx.compose.runtime.collectAsState // Import collectAsState for StateFlow

@Composable
fun SettingsScreen(
    navController: NavController,
    role: String,
    username: String,
    settingsViewModel: SettingsViewModel,
    sharedAuthViewModel: SharedAuthViewModel, // NEW: Inject SharedAuthViewModel
    onSaveSettings: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    context: Context
) {
    var newUsername by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordVisible1 by remember { mutableStateOf(false) }
    val status = settingsViewModel.changePasswordStatus
    val isLoading = settingsViewModel.isLoading

    // OBSERVE the token from SharedAuthViewModel instead of SharedPreferences directly
    val token by sharedAuthViewModel.token.collectAsState() // Use collectAsState() to observe StateFlow

    Scaffold(
        bottomBar = {
            NavBar(
                navController = navController,
                role = role,
                username = username
            )
        },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)

            // Old Password
            TextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Previous Password") },
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
                modifier = Modifier.fillMaxWidth()
            )

            // New Password
            TextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = if (passwordVisible1) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val imageVector = if (passwordVisible1)
                        ImageVector.vectorResource(id = R.drawable.baseline_visibility_24)
                    else
                        ImageVector.vectorResource(id = R.drawable.baseline_visibility_off_24)

                    val description = if (passwordVisible1) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible1 = !passwordVisible1 }) {
                        Icon(imageVector = imageVector, contentDescription = description)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Save Changes
            Button(
                onClick = {
                    // Check if the token is not empty (which implies it's valid/present)
                    if (token.isNotEmpty()) { // Use .isNotEmpty() for String, not != null
                        settingsViewModel.changePassword(token, oldPassword, newPassword)
                        onSaveSettings(newUsername, oldPassword, newPassword)
                    } else {
                        // Log error and show message
                        Log.e("SettingsScreen", "Authentication token is missing or empty.")
                        errorMessage = "Authentication required. Please log in again."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Saving..." else "Save Changes")
            }

            if (status.isNotBlank()) {
                Text(text = status, color = MaterialTheme.colorScheme.primary)
            }

            // Logout
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}