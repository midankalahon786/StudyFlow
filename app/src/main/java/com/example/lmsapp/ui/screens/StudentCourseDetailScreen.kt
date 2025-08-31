package com.example.lmsapp.ui.screens

import CourseViewModel
import android.util.Log // <--- Ensure this import is present
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lmsapp.ui.data.Course
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import com.example.lmsapp.ui.data.DataClasses.UIState
import com.example.lmsapp.ui.screens.HomeScreenUiElements.NavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCourseDetailScreen(
    navController: NavController,
    viewModel: CourseViewModel,
    courseId: String, // The courseId passed via navigation argument
    authViewModel: SharedAuthViewModel = viewModel(factory = SharedAuthViewModel.SharedAuthViewModelFactory(LocalContext.current))
) {
    // --- Logging for initial parameters and collected states ---
    Log.d("StudentCourseDetail", "Composing StudentCourseDetailScreen for courseId: $courseId")

    val token by authViewModel.token.collectAsState()
    val currentUserRole by authViewModel.role.collectAsState()
    val currentUsername by authViewModel.username.collectAsState()

    Log.d("StudentCourseDetail", "Collected Token: ${token.take(10)}... (length: ${token.length})") // Log first 10 chars to avoid revealing full token
    Log.d("StudentCourseDetail", "Collected User Role: $currentUserRole")
    Log.d("StudentCourseDetail", "Collected Username: $currentUsername")


    val courseState by viewModel.courses.collectAsState()
    val courseDetails by remember(courseState, courseId) {
        derivedStateOf {
            if (courseState is UIState.Success) {
                val foundCourse = (courseState as UIState.Success).data.find { it.id == courseId }
                Log.d("StudentCourseDetail", "Derived courseDetails for ID '$courseId': ${foundCourse?.title ?: "Not Found"}")
                foundCourse
            } else {
                Log.d("StudentCourseDetail", "CourseState is not Success: $courseState")
                null
            }
        }
    }

    val studentTabs = remember { listOf("Course Info", "Discussion","Resources") }
    var selectedStudentTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(courseId, token, selectedStudentTabIndex) {
        Log.d("StudentCourseDetail", "LaunchedEffect triggered with courseId: $courseId, token length: ${token.length}, selectedTabIndex: $selectedStudentTabIndex")

        if (selectedStudentTabIndex == 2) {
            Log.d("StudentCourseDetail", "Resources tab selected.")

            if (token.isNotBlank()) {
                Log.d("StudentCourseDetail", "Token is present. Calling viewModel.getCourseResources()")
                viewModel.loadCourseResources(courseId, token)
            } else {
                Log.e("StudentCourseDetail", "Token is blank. Cannot fetch resources.")
            }
        } else {
            Log.d("StudentCourseDetail", "Non-resources tab selected. Skipping resource fetch.")
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavBar(
                navController = navController,
                role = currentUserRole,
                username = currentUsername
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedStudentTabIndex) {
                studentTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedStudentTabIndex == index,
                        onClick = { selectedStudentTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (studentTabs[selectedStudentTabIndex]) {
                "Course Info" -> {
                    courseDetails?.let {
                        DisplayStudentCourseDetails(course = it)
                    } ?: run {
                        if (courseState is UIState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.wrapContentSize(align = androidx.compose.ui.Alignment.Center))
                            Log.d("StudentCourseDetail", "Course Info: Loading...")
                        } else if (courseState is UIState.Error) {
                            Text("Error loading course details: ${(courseState as UIState.Error).message}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp))
                            Log.e("StudentCourseDetail", "Course Info: Error loading: ${(courseState as UIState.Error).message}")
                        } else {
                            Text("Course details not found.", modifier = Modifier.padding(16.dp))
                            Log.d("StudentCourseDetail", "Course Info: Details not found.")
                        }
                    }
                }
                "Discussion" -> {
                    // --- Crucial logging for DiscussionForumScreen parameters ---
                    Log.d("StudentCourseDetail", "Navigating to Discussion tab. Passing courseId: $courseId")
                    DiscussionForumScreen(
                        navController = navController,
                        courseId = courseId, // Ensure this is the correct course ID string
                        sharedAuthViewModel = authViewModel
                    )
                }
                "Resources" -> {
                    val resourceState by viewModel.courseResources.collectAsState()

                    when (resourceState) {
                        is UIState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        }
                        is UIState.Error -> {
                            val error = (resourceState as UIState.Error).message
                            Text("Error loading resources: $error", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                        }
                        is UIState.Success -> {
                            val resources = (resourceState as UIState.Success).data
                            if (resources.isEmpty()) {
                                Text("No resources available for this course.", modifier = Modifier.padding(16.dp))
                            } else {
                                StudentCourseResourcesScreen(viewModel)
                            }
                        }
                        else -> {
                            Text("No resources found.", modifier = Modifier.padding(16.dp))
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun DisplayStudentCourseDetails(course: Course) {
    Log.d("StudentCourseDetail", "Displaying details for course: ${course.title} (ID: ${course.id})")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = course.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = course.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}