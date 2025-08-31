package com.example.lmsapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lmsapp.CourseDiscussionViewModel
import com.example.lmsapp.MainActivity
import com.example.lmsapp.ViewModels.SharedAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTopLevelCommentScreen(
    navController: NavController,
    courseId: String,
    discussionViewModel: CourseDiscussionViewModel = viewModel(),
    sharedAuthViewModel: SharedAuthViewModel = viewModel() // Inject SharedAuthViewModel
) {
    var content by remember { mutableStateOf(TextFieldValue("")) }
    val isLoading by discussionViewModel.isLoading.collectAsState()
    val errorMessage by discussionViewModel.errorMessage.collectAsState()

    // Pass the SharedAuthViewModel instance to the CourseDiscussionViewModel's factory
    val discussionViewModelFactory = remember {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CourseDiscussionViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return CourseDiscussionViewModel(
                        repository = (navController.context as MainActivity).lmsRepository, // Assuming MainActivity provides LMSRepository
                        sharedAuthViewModel = sharedAuthViewModel
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    val currentDiscussionViewModel: CourseDiscussionViewModel = viewModel(factory = discussionViewModelFactory)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Start New Discussion") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Discussion Content") },
                placeholder = { Text("Write your discussion post here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 10
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (errorMessage != null) {
                Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (content.text.isNotBlank()) {
                        // Create a new comment (top-level post)
                        currentDiscussionViewModel.postNewComment( // Use currentDiscussionViewModel
                            content = content.text,
                            courseId = courseId,
                            parentId = null
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = content.text.isNotBlank() && !isLoading
            ) {
                Text("Create Discussion Post")
            }
        }
    }
}