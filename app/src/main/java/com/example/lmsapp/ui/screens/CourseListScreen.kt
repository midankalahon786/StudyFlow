package com.example.lmsapp.ui.screens

import CourseViewModel
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lmsapp.ui.data.Course
import com.example.lmsapp.ui.data.DataClasses.UIState
import com.example.lmsapp.ui.screens.HomeScreenUiElements.NavBar
import com.example.lmsapp.ViewModels.SharedAuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    viewModel: CourseViewModel,
    authViewModel: SharedAuthViewModel,
    navController: NavController,
    role: String,
    username: String
) {
    val coursesState by viewModel.courses.collectAsState() // Renamed to coursesState
    val token by authViewModel.token.collectAsState()
    val isLoading by viewModel.isFetchingCourses.collectAsState()

    // Call fetchCoursesWithRole once when screen appears
    LaunchedEffect(Unit) {
        if (token.isNotBlank()) {
            if (role == "student") {
                viewModel.getMyCourses(token)
            } else {
                viewModel.fetchCoursesWithRole(token)
            }
        } else {
            Log.e("CourseListScreen", "Token is blank. Cannot fetch courses.")
        }
    }

    // Debugging the courses data
    Log.d("CourseListDebug", "Fetched Courses: $coursesState")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course List") },
                actions = {
                    if (role == "teacher") {
                        IconButton(onClick = { navController.navigate("addCourse") }) {
                            Log.d("Navigation", "Navigating to addCourse")
                            Icon(Icons.Default.Add, contentDescription = "Add Course")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavBar(
                navController = navController,
                role = role,
                username = username
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopStart
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                coursesState is UIState.Empty -> { // Correctly check for empty state
                    Text(
                        text = if (role == "student") "No courses assigned to you yet." else "No courses available.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                coursesState is UIState.Success -> {
                    val courses = (coursesState as UIState.Success).data // Extract data
                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        items(courses) { course ->
                            CourseCard(course = course) {
                                if (role == "student") {
                                    navController.navigate("studentCourseDetails/${course.id}") // Navigate to student course detail
                                } else {
                                    navController.navigate("courseDetails/${course.id}")
                                }

                            }
                        }
                    }
                }
                coursesState is UIState.Error -> {
                    val errorMessage = (coursesState as UIState.Error).message
                    Text(
                        text = "Error: $errorMessage", // Display the error message
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Text(
                        text = "Unknown state",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
fun CourseCard(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

