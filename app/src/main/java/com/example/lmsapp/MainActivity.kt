// app/MainActivity.kt (Modified)
package com.example.lmsapp

import AddCourseScreen
import CourseViewModel
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lmsapp.ui.screens.CourseDetailsScreen
import com.example.lmsapp.ui.screens.CourseListScreen
import com.example.lmsapp.ui.screens.CreateQuizScreen
import com.example.lmsapp.ui.screens.CreateTopLevelCommentScreen
import com.example.lmsapp.ui.screens.DiscussionForumScreen
import com.example.lmsapp.ui.screens.HomeScreen
import com.example.lmsapp.ui.screens.LoginScreen
import com.example.lmsapp.ui.screens.QuizDetailsScreen
import com.example.lmsapp.ui.screens.RegisterScreen
import com.example.lmsapp.ui.screens.ReportsScreen
import com.example.lmsapp.ui.screens.SettingsScreen
import com.example.lmsapp.ui.screens.StudentAllReportsScreen
import com.example.lmsapp.ui.screens.StudentCourseDetailScreen
import com.example.lmsapp.ui.screens.StudentQuizListScreen
import com.example.lmsapp.ui.screens.StudentQuizScreen
import com.example.lmsapp.ui.screens.TeacherCreateQuizScreen
import com.example.lmsapp.ui.screens.UpdateQuizScreen
import com.example.lmsapp.ViewModels.LoginViewModel
import com.example.lmsapp.ViewModels.RegisterViewModel
import com.example.lmsapp.ViewModels.ReportViewModel
import com.example.lmsapp.ViewModels.SettingsViewModel
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import com.example.lmsapp.ViewModels.StudentAllReportsViewModel
import com.example.lmsapp.ViewModels.StudentQuizDetailsViewModel
import com.example.lmsapp.ViewModels.StudentQuizListViewModel
import com.example.lmsapp.ViewModels.TeacherQuizViewModel
import com.example.lmsapp.ui.network.LMSRepository

import com.example.lmsapp.ui.theme.LMSAPPTheme

class MainActivity : ComponentActivity() {

    // Make LMSRepository a public val so it can be accessed by ViewModel factories
    val lmsRepository: LMSRepository by lazy {
        LMSRepository()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LMSAPPTheme {
                Column {
                    MainScreen(lmsRepository = lmsRepository) // Pass repository to MainScreen
                }
            }
        }
    }
}

// Main App Composable
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(lmsRepository: LMSRepository) { // Accept LMSRepository as a parameter
    val navController = rememberNavController()
    Log.d("NavDebug", "MainScreen NavController hash: ${navController.hashCode()}")
    val applicationContext = LocalContext.current.applicationContext

    val sharedAuthViewModel: SharedAuthViewModel = viewModel(
        factory = SharedAuthViewModel.SharedAuthViewModelFactory(applicationContext)
    )

    val loginViewModel = viewModel<LoginViewModel>(
        factory = LoginViewModel.LoginViewModelFactory(lmsRepository, sharedAuthViewModel) // Use passed lmsRepository
    )

    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current


    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModel.CourseViewModelFactory(
            lmsRepository, // Use passed lmsRepository
            savedStateRegistryOwner
        )
    )
    val registerViewModel = viewModel<RegisterViewModel>(
        factory = RegisterViewModel.RegisterViewModelFactory(lmsRepository) // Use passed lmsRepository
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.SettingsViewModelFactory(lmsRepository) // Use passed lmsRepository
    )

    val teacherQuizViewModel: TeacherQuizViewModel = viewModel(
        factory = TeacherQuizViewModel.TeacherQuizViewModelFactory(
            lmsRepository, // Use passed lmsRepository
            sharedAuthViewModel
        )
    )

    val studentQuizListViewModel: StudentQuizListViewModel = viewModel(
        factory = StudentQuizListViewModel.StudentQuizListViewModelFactory(lmsRepository) // Use passed lmsRepository
    )

    val reportViewModel: ReportViewModel = viewModel(
        factory = ReportViewModel.ReportViewModelFactory(lmsRepository) // Use passed lmsRepository
    )

    val studentAllReportsViewModel: StudentAllReportsViewModel = viewModel(
        factory = StudentAllReportsViewModel.StudentAllReportsViewModelFactory(lmsRepository) // Use passed lmsRepository
    )

    // NEW: CourseDiscussionViewModel Factory
    val courseDiscussionViewModelFactory = remember {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CourseDiscussionViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return CourseDiscussionViewModel(
                        repository = lmsRepository, // Pass the lmsRepository
                        sharedAuthViewModel = sharedAuthViewModel // Pass the sharedAuthViewModel
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }


    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                sharedAuthViewModel = sharedAuthViewModel,
                onNavigateToNext = {
                    val role = sharedAuthViewModel.role.value
                    val username = sharedAuthViewModel.username.value
                    if (role == "teacher") {
                        navController.navigate("home/teacher/$username")
                    } else {
                        navController.navigate("home/student/$username")
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.popBackStack() // back to login after successful registration
                },
                onBackToLogin = {
                    navController.popBackStack() // back to login when "Back to Login" is clicked
                },
                token = sharedAuthViewModel.token.collectAsState().value
            )
        }

        // Course list screen
        composable("courses/{role}/{username}") { backStackEntry ->
            val role = sharedAuthViewModel.role.collectAsState().value
            val username = sharedAuthViewModel.username.collectAsState().value

            CourseListScreen(
                viewModel = courseViewModel,
                navController = navController,
                role = role,
                username = username,
                authViewModel = sharedAuthViewModel
            )
        }
        composable("addCourse") {
            val role = sharedAuthViewModel.role.collectAsState().value

            if (role == "teacher") {
                // If the user is a teacher, allow them to add a course
                AddCourseScreen(
                    navController = navController,
                    viewModel = courseViewModel,
                    authViewModel = sharedAuthViewModel,
                    isButtonEnabled = true // Enable the button for teacher
                )
            } else {
                // If the user is not a teacher, navigate them back or show a restricted message
                Log.d("NavDebug", "Access denied. Only teachers can add a course.")
                AddCourseScreen(
                    navController = navController,
                    viewModel = courseViewModel,
                    authViewModel = sharedAuthViewModel,
                    isButtonEnabled = false // Disable the button for student
                )
                navController.popBackStack()
            }
        }

        composable("home/{role}/{username}") { backStackEntry ->
            val role = sharedAuthViewModel.role.collectAsState().value
            val username = sharedAuthViewModel.username.collectAsState().value

            HomeScreen(
                navController = navController, // Pass navController
                role = role,
                username = username,
                onViewCourses = {
                    Log.d("NavDebug", "HomeScreen -> onViewCourses")
                    navController.navigate("courses/$role/$username") // Pass role and username to navigate
                },
                onLogout = {
                    loginViewModel.logout()
                    navController.popBackStack("login", inclusive = false)
                }
            )
        }

        // Add course screen for teachers
        composable("upload_question_bank") {
            CreateQuizScreen(
                quizViewModel = teacherQuizViewModel,
                navController = navController,
                username = sharedAuthViewModel.username.toString(), // Adjust as per your ViewModel
                authViewModel = sharedAuthViewModel,
            )
        }

        composable("quiz") {
            // Collect values from the sharedAuthViewModel's state
            val role = sharedAuthViewModel.role.collectAsState().value
            val username = sharedAuthViewModel.username.collectAsState().value
            val token = sharedAuthViewModel.token.collectAsState().value

            // Check the role and navigate accordingly
            if (role == "teacher") {
                TeacherCreateQuizScreen(
                    navController = navController,
                    teacherQuizViewModel = teacherQuizViewModel,
                    sharedAuthViewModel = sharedAuthViewModel
                )
            } else {
                StudentQuizListScreen(
                    navController = navController,
                    username = username,
                    token = token,
                    viewModel = studentQuizListViewModel,
                    sharedAuthViewModel = sharedAuthViewModel
                )
            }
        }

        // Quiz Details screen
        composable(
            route = "quizDetails/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getInt("quizId")
            val role = sharedAuthViewModel.role.collectAsState().value
            val studentQuizDetailsViewModel: StudentQuizDetailsViewModel = viewModel(
                factory = StudentQuizDetailsViewModel.StudentQuizDetailsViewModelFactory(
                    lmsRepository // Use passed lmsRepository
                )
            )

            QuizDetailsScreen(
                quizId = quizId,
                viewModel = studentQuizDetailsViewModel,
                navController = navController,
                userRole = role,
                sharedAuthViewModel = sharedAuthViewModel

            )
        }

        composable(
            route = "studentQuiz/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getInt("quizId")
            val token = sharedAuthViewModel.token.collectAsState().value // Get the token for API calls

            if (quizId != null) {
                StudentQuizScreen(
                    token = token,
                    quizId = quizId,
                    navController = navController,
                    sharedAuthViewModel = sharedAuthViewModel
                )
            } else {
                Text("Error: Quiz ID or Token missing.")
            }
        }


        // Course Details Screen
        composable("courseDetails/{courseId}") { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            CourseDetailsScreen(
                courseId = courseId.toString(),
                viewModel = courseViewModel,
                authViewModel = sharedAuthViewModel,
                navController = navController
            )
        }

        //Student Course Details Screen
        composable("studentCourseDetails/{courseId}"){ backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            StudentCourseDetailScreen(
                navController = navController,
                viewModel = courseViewModel,
                courseId = courseId.toString(),
                authViewModel = sharedAuthViewModel
            )
        }

        // Settings screen
        composable(
            "settings/{role}/{username}",
            arguments = listOf(
                navArgument("role") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Get arguments from the back stack entry
            val role = backStackEntry.arguments?.getString("role") ?: "default_role"
            val username = backStackEntry.arguments?.getString("username") ?: "default_username"
            val context = LocalContext.current

            SettingsScreen(
                navController = navController,
                role = role,
                username = username,
                settingsViewModel = settingsViewModel,
                onSaveSettings = { newUsername, oldPassword, newPassword ->
                    // Handle saving settings logic here, such as calling the API or updating data
                    Log.d(
                        "Settings",
                        "New username: $newUsername, Old password: $oldPassword, New password: $newPassword"
                    )
                },
                onLogout = {
                    // Perform the logout action
                    loginViewModel.logout()
                    // Navigate to the login screen after logging out
                    navController.popBackStack("login", inclusive = false)
                },
                context = context,
                sharedAuthViewModel = sharedAuthViewModel
            )
        }

        composable(
            route = "studentreportsscreen/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getInt("quizId")
            val token = sharedAuthViewModel.token.collectAsState().value
            val userId = sharedAuthViewModel.userId.collectAsState().value

            if (quizId != null && token.isNotEmpty() && userId != null) {
                ReportsScreen(
                    navController = navController,
                    reportViewModel = reportViewModel,
                    quizId = quizId,
                    sharedAuthViewModel = sharedAuthViewModel,
                    userId = userId
                )
            } else {
                Text("Error: Report data missing (Quiz ID: $quizId, Token present: ${token.isNotEmpty()}, User ID: $userId).")
                Log.e("StudentReportsScreen", "Error: Report data missing for StudentReportsScreen. Quiz ID: $quizId, Token present: ${token.isNotEmpty()}, User ID: $userId")
            }
        }

        composable(
            route = "studentallreports/{studentId}",
            arguments = listOf(navArgument("studentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId")
            val token = sharedAuthViewModel.token.collectAsState().value

            if (studentId != null && token.isNotEmpty()) {
                StudentAllReportsScreen(
                    navController = navController,
                    studentAllReportsViewModel = studentAllReportsViewModel,
                    sharedAuthViewModel = sharedAuthViewModel, // Pass for token/userId access within the screen if needed
                    studentId = studentId
                )
            } else {
                Text("Error: Student ID or Token missing for All Reports Screen.")
                Log.e("StudentAllReportsScreen", "Error: Student ID ($studentId) or Token (${token.isNotEmpty()}) missing for All Reports Screen.")
            }
        }

        composable("editQuizScreen/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getInt("quizId") ?: 0 // Get the quizId
            if (quizId != 0) {
                UpdateQuizScreen(navController = navController, quizId = quizId, teacherQuizViewModel,sharedAuthViewModel)
            } else {
                // Handle error, e.g., show a message or navigate back
                Text("Error: Quiz ID not found.", modifier = Modifier.padding(16.dp))
            }
        }

        // NEW: Discussion Forum Routes
        composable(
            route = "discussionForumScreen/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            DiscussionForumScreen(
                navController = navController,
                courseId = courseId,
                sharedAuthViewModel = sharedAuthViewModel // Pass sharedAuthViewModel
            )
        }


        composable(
            route = "createTopLevelCommentScreen/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            CreateTopLevelCommentScreen(
                navController = navController,
                courseId = courseId,
                discussionViewModel = viewModel(factory = courseDiscussionViewModelFactory), // Use the factory
                sharedAuthViewModel = sharedAuthViewModel // Pass sharedAuthViewModel
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(lmsRepository = LMSRepository()) // Provide a dummy repository for preview
}
