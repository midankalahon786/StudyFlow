import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lmsapp.ui.data.DataClasses.UIState
import com.example.lmsapp.ui.screens.HomeScreenUiElements.NavBar
import com.example.lmsapp.ui.network.FileUtils
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import kotlinx.coroutines.launch

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseScreen(
    navController: NavController,
    viewModel: CourseViewModel,
    authViewModel: SharedAuthViewModel,
    isButtonEnabled: Boolean
) {
    val context = LocalContext.current
    val token by authViewModel.token.collectAsState()
    val isCreating by viewModel.isCreatingCourse.collectAsState()
    val creationResult by viewModel.courseCreationResult.collectAsState()
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var assignedUsers by remember { mutableStateOf<List<Int>>(emptyList()) }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var showUserDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fileUri = uri
        Log.d("AddCourseScreen", "File selected: ${uri?.path}")
    }

    // Fetch students only once per token session
    LaunchedEffect(token) {
        Log.d("AddCourseScreen", "Fetching students with token: $token")
        viewModel.fetchStudents(token)
    }

    val studentListState by viewModel.studentList.collectAsState() // Collect as State
    val studentList = remember(studentListState) {
        if (studentListState is UIState.Success) {
            (studentListState as UIState.Success).data
        } else {
            emptyList()
        }
    }

    // Refresh student list logic
    fun refreshStudentList() {
        // Calling within a coroutine scope to call suspend functions correctly
        scope.launch {
            viewModel.resetStudentCache() // Clear the cache
            viewModel.fetchStudents(token) // Re-fetch students
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Course") },
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
                role = authViewModel.role.value,
                username = authViewModel.username.value
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Course title and description fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Course Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            // File picker button
            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(text = fileUri?.lastPathSegment ?: "Choose File")
            }

            // Assign students button
            Button(
                onClick = { showUserDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = isButtonEnabled
            ) {
                Text("Assign Students")
            }

            // Display assigned students as chips with refresh button
            if (assignedUsers.isNotEmpty()) {
                Text("Assigned Students:", modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val assignedEnrollmentNumbers = remember(assignedUsers, studentList) {
                        assignedUsers.mapNotNull { selectedId ->
                            studentList.find { it.id == selectedId }?.enrollmentNumber
                        }
                    }

                    assignedEnrollmentNumbers.forEach { enrollmentNum ->
                        AssistChip(
                            onClick = {},
                            label = { Text(enrollmentNum) }
                        )
                    }

                    // Refresh button to reload the student list
                    IconButton(onClick = { refreshStudentList() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Students List"
                        )
                    }
                }
            }

            // Student selection dialog
            if (showUserDialog) {
                AlertDialog(
                    onDismissRequest = { showUserDialog = false },
                    confirmButton = {
                        Button(onClick = { showUserDialog = false }) {
                            Text("Done")
                        }
                    },
                    title = { Text("Select Students") },
                    text = {
                        Column {
                            if (studentListState is UIState.Loading) {
                                Text("Loading Students...")
                            } else if (studentListState is UIState.Error) {
                                Text("Error loading students.")
                            } else if (studentList.isEmpty()) {
                                Text("No students available.")
                            }
                            else{
                                studentList.forEach { student ->
                                    val isSelected = assignedUsers.contains(student.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                assignedUsers = if (isSelected) {
                                                    assignedUsers - student.id
                                                } else {
                                                    assignedUsers + student.id
                                                }
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = {
                                                assignedUsers = if (it) {
                                                    assignedUsers + student.id
                                                } else {
                                                    assignedUsers - student.id
                                                }
                                            }
                                        )

                                        Text(
                                            text = "${student.firstName} ${student.lastName} (${student.enrollmentNumber})",
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .clickable {
                                                    // Toggle selection when the enrollment number is tapped directly
                                                    assignedUsers = if (isSelected) {
                                                        assignedUsers - student.id
                                                    } else {
                                                        assignedUsers + student.id
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // Create course button
            Button(
                onClick = {
                    scope.launch {
                        val filePart = fileUri?.let {
                            FileUtils.createMultipartFromUri(context, it, "file")
                        }

                        viewModel.createCourseWithExtras(
                            token = token,
                            title = title,
                            description = description,
                            assignedUsers = assignedUsers,
                            file = filePart
                        )
                    }
                },
                enabled = isButtonEnabled && !isCreating,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Create Course")
            }

            // Displaying course creation progress or result
            if (isCreating) {
                Text(
                    "Creating course...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            creationResult?.let { result ->
                if (result is UIState.Success) {
                    Text(
                        "Course created successfully!",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LaunchedEffect(result) {
                        navController.popBackStack()
                    }
                } else if (result is UIState.Error) {
                    Text(
                        "Failed to create course: ${result.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
