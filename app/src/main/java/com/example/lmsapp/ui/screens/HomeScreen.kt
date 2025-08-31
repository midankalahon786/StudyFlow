package com.example.lmsapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // New import
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api // New import
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices.PIXEL_7_PRO
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lmsapp.ui.data.Course
import com.example.lmsapp.ui.data.DataClasses.Student
import com.example.lmsapp.ui.screens.HomeScreenUiElements.NavBar
import com.example.lmsapp.ui.screens.HomeScreenUiElements.TopBar

@Composable
fun HomeScreen(
    navController: NavController,
    role: String,
    username: String,
    recentlyViewedCourses: List<Course>, // New parameter for course data
    onViewCourses: () -> Unit,
    onLogout: () -> Unit,
    onCourseClick: (String) -> Unit // New parameter for handling clicks
) {

    Log.d("NavDebug", "HomeScreen NavController hash: ${navController.hashCode()}")
    Scaffold(
        topBar = {
            TopBar(username = username)
        },
        bottomBar = {
            NavBar(
                navController = navController,
                role = role,
                username = username
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing

    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Log.d("Debug", "Received username in HomeScreen: $username")
                Text(
                    text = "Welcome, $username!",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                Text(
                    text = "You are logged in as a $role.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // --- NEW: Recently Viewed Courses Section ---
            item {
                Text(
                    text = "Recently Viewed Courses",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (recentlyViewedCourses.isEmpty()) {
                item {
                    Text(
                        text = "You haven't viewed any courses yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(recentlyViewedCourses) { course ->
                    CourseItem(course = course, onClick = { onCourseClick(course.id) })
                }
            }
            // --- END of new section ---


            item {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp) // Added padding for better spacing
                ) {
                    Text("Logout")
                }
            }
        }
    }
}


// --- NEW: Composable for a single course item ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseItem(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = course.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}


@Preview(showBackground = true, showSystemUi = true, device = PIXEL_7_PRO)
@Composable
fun HomeScreenPreview() {
    val dummyNavController = rememberNavController()
    val sampleCourses = listOf(
        Course(
            "1", "Introduction to Kotlin",
            description = "Kotlin for beginners",
            fileUrl = "",
            createdBy = 1,
            createdAt = "2",
            updatedAt = "3",
            teacherId = 4,
            students = listOf<Student>()
        )

    )
    HomeScreen(
        navController = dummyNavController,
        role = "teacher",
        username = "John Doe",
        recentlyViewedCourses = sampleCourses, // Pass the new data
        onViewCourses = {},
        onLogout = {},
        onCourseClick = { courseId -> Log.d("PreviewClick", "Clicked on course $courseId") } // Handle clicks
    )
}

// --- NEW: Added a preview for the empty state ---
@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyRecentsPreview() {
    val dummyNavController = rememberNavController()
    HomeScreen(
        navController = dummyNavController,
        role = "student",
        username = "Jane Doe",
        recentlyViewedCourses = emptyList(), // Empty list
        onViewCourses = {},
        onLogout = {},
        onCourseClick = {}
    )
}