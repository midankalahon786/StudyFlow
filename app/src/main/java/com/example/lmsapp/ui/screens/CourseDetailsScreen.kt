package com.example.lmsapp.ui.screens

import CourseViewModel
import ResourceType
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lmsapp.ui.data.DataClasses.Resource
import com.example.lmsapp.ui.data.DataClasses.Student
import com.example.lmsapp.ui.data.DataClasses.UIState
import com.example.lmsapp.ui.network.FileUtils
import com.example.lmsapp.R
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import com.example.lmsapp.ui.network.formatDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailsScreen(
    courseId: String,
    viewModel: CourseViewModel,
    authViewModel: SharedAuthViewModel = viewModel(
        factory = SharedAuthViewModel.SharedAuthViewModelFactory(
            LocalContext.current
        )
    ),
    navController: NavController,
) {
    val token by authViewModel.token.collectAsState()
    val currentUserRole by authViewModel.role.collectAsState()
    val studentsInCourseState by viewModel.studentList.collectAsState()
    val studentsInCourse = remember(studentsInCourseState) {
        if (studentsInCourseState is UIState.Success) {
            (studentsInCourseState as UIState.Success).data
        } else emptyList()
    }
    val coroutineScope = rememberCoroutineScope()

    val allStudents by viewModel.students.collectAsState()
    val availableStudents = remember(allStudents, studentsInCourse) {
        if (allStudents is UIState.Success) {
            (allStudents as UIState.Success<List<Student>>).data.filter { student ->
                studentsInCourse.none { it.id == student.id }
            }
        } else {
            emptyList()
        }
    }
    val selectedStudentsToAdd = remember { mutableStateListOf<Student>() }
    val selectedStudentsToRemove = remember { mutableStateListOf<Student>() }
    val showDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    val tabs = remember {
        mutableStateListOf(
            "Discussion",
            "Resources" // Add Resources tab
        ).apply {
            if (currentUserRole == "teacher") {
                add(0, "Manage Students") // Keep Manage Students first for teachers
            }
        }
    }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Adjust selectedTabIndex logic if "Manage Students" is dynamically added
    LaunchedEffect(currentUserRole) {
        // If teacher, and the default 0 was discussion, ensure it moves to 1
        if (currentUserRole == "teacher" && tabs[selectedTabIndex] == "Discussion") {
            selectedTabIndex = tabs.indexOf("Manage Students") // Should be 0
        }
    }

    LaunchedEffect(courseId, token, selectedTabIndex) {
        if (token.isNotBlank()) {
            when (tabs[selectedTabIndex]) {
                "Manage Students" -> {
                    if (currentUserRole == "teacher") {
                        viewModel.getStudentsByCourseId(courseId, token)
                    }
                }
                "Resources" -> {
                    viewModel.loadCourseResources(courseId, token) // Load resources when tab is selected
                }
            }
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
                },
                actions = {
                    if (currentUserRole == "teacher") {
                        IconButton(onClick = { showDeleteDialog.value = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Course")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Maintain this spacer for separation

            when (tabs[selectedTabIndex]) {
                "Manage Students" -> {
                    ManageCourseStudentsContent(
                        innerPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        studentsInCourseState = studentsInCourseState,
                        studentsInCourse = studentsInCourse,
                        availableStudents = availableStudents,
                        showDialog = showDialog,
                        selectedStudentsToAdd = selectedStudentsToAdd,
                        selectedStudentsToRemove = selectedStudentsToRemove,
                        coroutineScope = coroutineScope,
                        viewModel = viewModel,
                        courseId = courseId,
                        token = token
                    )
                }
                "Discussion" -> {
                    DiscussionForumScreen(
                        navController = navController,
                        courseId = courseId,
                        sharedAuthViewModel = authViewModel
                    )
                }
                "Resources" -> {
                    CourseResourcesContent(
                        courseId = courseId,
                        viewModel = viewModel,
                        token = token,
                        currentUserRole = currentUserRole // Pass role for teacher actions
                    )
                }
            }
        }
    }

    if (showDialog.value) {
        ManageStudentsDialog(
            onDismissRequest = { showDialog.value = false },
            availableStudents = viewModel.students,
            studentsInCourse = studentsInCourse,
            selectedStudentsToAdd = selectedStudentsToAdd,
            selectedStudentsToRemove = selectedStudentsToRemove,
            onConfirm = {
                coroutineScope.launch {
                    val studentsAfterRemoval = studentsInCourse.filter { it !in selectedStudentsToRemove }
                    val studentsToUpdate = studentsAfterRemoval + selectedStudentsToAdd.toList()
                    viewModel.updateCourseStudents(courseId, token, studentsToUpdate)
                    showDialog.value = false
                    selectedStudentsToAdd.clear()
                    selectedStudentsToRemove.clear()
                    viewModel.getStudentsByCourseId(courseId, token)
                }
            },
            onCancel = {
                showDialog.value = false
                selectedStudentsToAdd.clear()
                selectedStudentsToRemove.clear()
            },
            onDeleteStudent = { student ->
                coroutineScope.launch {
                    viewModel.deleteStudentFromCourse(courseId, token, student.id)
                    viewModel.getStudentsByCourseId(courseId, token)
                }
            },
            viewModel = viewModel,
            token = token,
            courseId = courseId
        )
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Delete Course") },
            text = { Text("Are you sure you want to delete this course?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteCourse(courseId, token)
                            navController.popBackStack()
                        }
                        showDeleteDialog.value = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CourseResourcesContent(
    courseId: String,
    viewModel: CourseViewModel,
    token: String,
    currentUserRole: String?,
) {
    val resourcesState by viewModel.courseResources.collectAsState()
    val context = LocalContext.current // For opening URLs

    var showAddResourceDialog by remember { mutableStateOf(false) }
    var showEditResourceDialog by remember { mutableStateOf(false) }
    var selectedResourceToEdit by remember { mutableStateOf<Resource?>(null) }

    // Filter and Sort states
    var filterType by remember { mutableStateOf<ResourceType?>(null) }
    var sortBy by remember { mutableStateOf("newest") } // "newest", "oldest", "title_asc", "title_desc"
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredAndSortedResources = remember(resourcesState, filterType, sortBy) {
        val resources = (resourcesState as? UIState.Success)?.data.orEmpty()

        val filtered = if (filterType == null) resources else resources.filter { it.type == filterType }

        when (sortBy) {
            "newest" -> filtered.sortedByDescending { LocalDateTime.parse(it.createdAt, DateTimeFormatter.ISO_DATE_TIME) }
            "oldest" -> filtered.sortedBy { LocalDateTime.parse(it.createdAt, DateTimeFormatter.ISO_DATE_TIME) }
            "title_asc" -> filtered.sortedBy { it.title.lowercase() }
            "title_desc" -> filtered.sortedByDescending { it.title.lowercase() }
            else -> filtered
        }
    }

    LaunchedEffect(resourcesState) {
        if (resourcesState is UIState.Success) {
            Log.d("ResourcesCheck", "Loaded resources: ${(resourcesState as UIState.Success).data}")
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (currentUserRole == "teacher") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { showAddResourceDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Resource")
                    Spacer(Modifier.width(4.dp))
                    Text("Add Resource")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Filter and Sort Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Button(onClick = { showFilterMenu = true }) {
                    Icon(painter = painterResource(id = R.drawable.outline_filter_list_24), contentDescription = "Filter")
                    Spacer(Modifier.width(4.dp))
                    Text("Filter")
                }
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false }
                ) {
                    DropdownMenuItem(text = { Text("All Types") }, onClick = {
                        filterType = null
                        showFilterMenu = false
                    })
                    ResourceType.entries.forEach { type ->
                        DropdownMenuItem(text = { Text(type.name.replace("_", " ")) }, onClick = {
                            filterType = type
                            showFilterMenu = false
                        })
                    }
                }
            }

            Box {
                Button(onClick = { showSortMenu = true }) {
                    Icon(painter = painterResource((R.drawable.baseline_sort_24)), contentDescription = "Sort")
                    Spacer(Modifier.width(4.dp))
                    Text("Sort")
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(text = { Text("Newest First") }, onClick = { sortBy = "newest"; showSortMenu = false })
                    DropdownMenuItem(text = { Text("Oldest First") }, onClick = { sortBy = "oldest"; showSortMenu = false })
                    DropdownMenuItem(text = { Text("Title (A-Z)") }, onClick = { sortBy = "title_asc"; showSortMenu = false })
                    DropdownMenuItem(text = { Text("Title (Z-A)") }, onClick = { sortBy = "title_desc"; showSortMenu = false })
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (resourcesState) {
            is UIState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UIState.Error -> {
                Text(
                    text = "Error loading resources: ${(resourcesState as UIState.Error).message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            is UIState.Success -> {
                if (filteredAndSortedResources.isEmpty()) {
                    Text("No resources found for this course ${if (filterType != null) " (filtered by ${filterType?.name})" else ""}.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredAndSortedResources) { resource ->
                            ResourceCard(
                                resource = resource,
                                isTeacher = currentUserRole == "teacher",
                                onEdit = {
                                    selectedResourceToEdit = it
                                    showEditResourceDialog = true
                                },
                                onDelete = { viewModel.deleteCourseResource(it.resourceId, token,
                                    courseId) },
                                onOpenLink = { url ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.e("ResourceCard", "Could not open URL: $url", e)
                                        // Optionally show a toast error
                                    }
                                }
                            )
                        }
                    }
                }
            }

            UIState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No resources available.", style = MaterialTheme.typography.bodyLarge)
                }
            }

        }
    }

    if (showAddResourceDialog) {
        AddEditResourceDialog(
            resource = null, // Null for adding
            courseId = courseId,
            token = token,
            viewModel = viewModel,
            onDismiss = { showAddResourceDialog = false }
        )
    }

    if (showEditResourceDialog && selectedResourceToEdit != null) {
        AddEditResourceDialog(
            resource = selectedResourceToEdit, // Pass resource for editing
            courseId = courseId,
            token = token,
            viewModel = viewModel,
            onDismiss = {
                showEditResourceDialog = false
                selectedResourceToEdit = null
            }
        )
    }
}

@Composable
fun ResourceCard(
    resource: Resource,
    isTeacher: Boolean,
    onEdit: (Resource) -> Unit,
    onDelete: (Resource) -> Unit,
    onOpenLink: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            resource.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Type: ${resource.type?.name?.replace("_", " ") ?: resource.friendlyType ?: "Unknown"}",
                style = MaterialTheme.typography.labelSmall
            )


            resource.url?.let { url ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Link: $url",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onOpenLink(url) }
                )
            }
            resource.content?.let { content ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Content: $content", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Uploaded by: ${resource.uploadedBy?.username?:"Unknown"} on ${formatDate
                    (resource
                    .createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            if (isTeacher) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { onEdit(resource) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Resource")
                    }
                    IconButton(onClick = { onDelete(resource) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Resource", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditResourceDialog(
    resource: Resource?, // Null for add, non-null for edit
    courseId: String,
    token: String,
    viewModel: CourseViewModel,
    onDismiss: () -> Unit,
) {
    val isEditMode = resource != null

    var title by remember { mutableStateOf(resource?.title ?: "") }
    var description by remember { mutableStateOf(resource?.description ?: "") }
    var type by remember { mutableStateOf(resource?.type ?: ResourceType.OTHER) }
    var url by remember { mutableStateOf(resource?.url ?: "") }
    var content by remember { mutableStateOf(resource?.content ?: "") }
    var tagsInput by remember { mutableStateOf(resource?.tags?.joinToString(", ") ?: "") }

    var selectedFileUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedFileName by remember { mutableStateOf(resource?.fileName ?: "") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedFileUri = uri
        selectedFileName = uri?.let { FileUtils.getFileName(context, it) } ?: ""
    }

    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Resource" else "Add New Resource") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = title.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Resource type dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = type.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Resource Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ResourceType.entries.forEach { resourceType ->
                            DropdownMenuItem(
                                text = { Text(resourceType.name) },
                                onClick = {
                                    type = resourceType
                                    expanded = false
                                    when (resourceType) {
                                        ResourceType.DOCUMENT, ResourceType.OTHER -> {
                                            content = ""
                                            url = ""
                                        }
                                        ResourceType.VIDEO, ResourceType.LINK -> {
                                            selectedFileUri = null
                                            selectedFileName = ""
                                            content = ""
                                        }
                                        ResourceType.NOTE -> {
                                            selectedFileUri = null
                                            selectedFileName = ""
                                            url = ""
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (type) {
                    ResourceType.DOCUMENT, ResourceType.OTHER -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { pickFileLauncher.launch("*/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Select File")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedFileName.ifEmpty { "No file selected" },
                                modifier = Modifier.weight(2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (type == ResourceType.OTHER) {
                            OutlinedTextField(
                                value = url,
                                onValueChange = { url = it },
                                label = { Text("Optional URL (if no file)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    ResourceType.VIDEO, ResourceType.LINK -> {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("URL") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            modifier = Modifier.fillMaxWidth(),
                            isError = url.isBlank()
                        )
                        selectedFileUri = null
                        selectedFileName = ""
                    }

                    ResourceType.NOTE -> {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Note Content") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            minLines = 5,
                            isError = content.isBlank()
                        )
                        selectedFileUri = null
                        selectedFileName = ""
                        url = ""
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma-separated, optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val filePart = selectedFileUri?.let {
                        FileUtils.createMultipartFromUri(context, it, "resourceFile")
                    }

                    val typeValue = type.backendType // e.g. "document"
                    val friendlyType = type.name // e.g. "DOCUMENT"

                    coroutineScope.launch {
                        if (isEditMode) {
                            viewModel.updateCourseResource(
                                resourceId = resource.resourceId,
                                token = token,
                                title = title,
                                description = description.takeIf { it.isNotBlank() },
                                type = typeValue,
                                friendlyType = friendlyType,
                                url = url.takeIf { it.isNotBlank() },
                                content = content.takeIf { it.isNotBlank() },
                                tags = tagsList.takeIf { it.isNotEmpty() },
                                filePart = filePart,
                                courseId = courseId
                            )
                        } else {
                            viewModel.addCourseResource(
                                courseId = courseId,
                                token = token,
                                title = title,
                                description = description.takeIf { it.isNotBlank() },
                                type = typeValue,
                                friendlyType = friendlyType,
                                url = url.takeIf { it.isNotBlank() },
                                content = content.takeIf { it.isNotBlank() },
                                tags = tagsList.takeIf { it.isNotEmpty() },
                                filePart = filePart
                            )
                        }
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() &&
                        when (type) {
                            ResourceType.NOTE -> content.isNotBlank()
                            ResourceType.VIDEO, ResourceType.LINK -> url.isNotBlank()
                            ResourceType.DOCUMENT, ResourceType.OTHER ->
                                selectedFileUri != null ||
                                        (isEditMode && resource?.filePath != null && selectedFileUri == null) ||
                                        (type == ResourceType.OTHER && url.isNotBlank())
                        }
            ) {
                Text(if (isEditMode) "Update" else "Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@Composable
fun ManageStudentsButton(showDialog: MutableState<Boolean>) {
    Button(onClick = { showDialog.value = true }) {
        Text("Manage Students")
    }
}

@Composable
fun ManageCourseStudentsContent(
    innerPadding: PaddingValues,
    studentsInCourseState: UIState<List<Student>>,
    studentsInCourse: List<Student>,
    availableStudents: List<Student>,
    showDialog: MutableState<Boolean>,
    selectedStudentsToAdd: MutableList<Student>,
    selectedStudentsToRemove: MutableList<Student>,
    coroutineScope: CoroutineScope,
    viewModel: CourseViewModel,
    courseId: String,
    token: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Column {
            ManageStudentsButton(showDialog = showDialog)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Current Students in Course", style = MaterialTheme.typography.titleMedium)
            if (studentsInCourseState is UIState.Loading) {
                CircularProgressIndicator()
            } else if (studentsInCourseState is UIState.Error) {
                Text("Error loading students.")
            } else if (studentsInCourse.isEmpty()) {
                Text("No students currently assigned to this course.")
            } else {
                LazyColumn {
                    items(studentsInCourse) { student ->
                        Text("- ${student.firstName} ${student.lastName} (${student.enrollmentNumber})")
                    }
                }
            }
        }
    }
}

@Composable
fun ManageStudentsDialog(
    onDismissRequest: () -> Unit,
    availableStudents: StateFlow<UIState<List<Student>>>,
    studentsInCourse: List<Student>,
    selectedStudentsToAdd: MutableList<Student>,
    selectedStudentsToRemove: MutableList<Student>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDeleteStudent: (Student) -> Unit,
    viewModel: CourseViewModel,
    token: String,
    courseId: String,
) {
    val showAddStudentsDialog = remember { mutableStateOf(false) }
    val showRemoveStudentsDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(showAddStudentsDialog.value) {
        if (showAddStudentsDialog.value) {
            viewModel.fetchStudents(token)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Manage Students") },
        text = {
            Column {
                Button(onClick = { showAddStudentsDialog.value = true }) {
                    Text("Add Students")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showRemoveStudentsDialog.value = true }) {
                    Text("Remove Students")
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Update")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
        modifier = Modifier.wrapContentWidth()
    )

    if (showAddStudentsDialog.value) {
        AddStudentsDialog(
            onDismissRequest = { showAddStudentsDialog.value = false },
            availableStudents = viewModel.students,
            studentsInCourse = studentsInCourse,
            selectedStudentsToAdd = selectedStudentsToAdd,
            selectedStudentsToRemove = selectedStudentsToRemove,
            onConfirm = {
                showAddStudentsDialog.value = false
            },
            coroutineScope = coroutineScope,
            viewModel = viewModel,
            token = token,
            courseId = courseId
        )
    }

    if (showRemoveStudentsDialog.value) {
        RemoveStudentsDialog(
            onDismissRequest = { showRemoveStudentsDialog.value = false },
            studentsInCourse = studentsInCourse,
            selectedStudentsToRemove = selectedStudentsToRemove,
            selectedStudentsToAdd = selectedStudentsToAdd,
            onDeleteStudent = onDeleteStudent,
            onConfirm = {
                showRemoveStudentsDialog.value = false
            }
        )
    }
}

@Composable
fun AddStudentsDialog(
    onDismissRequest: () -> Unit,
    availableStudents: StateFlow<UIState<List<Student>>>,
    selectedStudentsToAdd: MutableList<Student>,
    selectedStudentsToRemove: MutableList<Student>,
    onConfirm: () -> Unit,
    coroutineScope: CoroutineScope,
    viewModel: CourseViewModel,
    token: String,
    studentsInCourse: List<Student>,
    courseId: String,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Add Students") },
        text = {
            val fetchedAllStudentsState: UIState<List<Student>> = availableStudents.collectAsState(initial = UIState.Loading).value
            when (fetchedAllStudentsState) {
                is UIState.Loading -> {
                    CircularProgressIndicator()
                }
                is UIState.Error -> {
                    Text("Error: ${fetchedAllStudentsState.message}")
                }
                is UIState.Success -> {
                    val students = fetchedAllStudentsState.data
                    val studentsNotYetInCourse = students.filter { student ->
                        studentsInCourse.none { it.id == student.id }
                    }

                    if (studentsNotYetInCourse.isEmpty()) {
                        Text("No new students available to add.")
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(studentsNotYetInCourse) { student ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedStudentsToAdd.contains(student),
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                selectedStudentsToAdd.add(student)
                                                selectedStudentsToRemove.remove(student)
                                            } else {
                                                selectedStudentsToAdd.remove(student)
                                            }
                                        }
                                    )
                                    Text("${student.firstName} ${student.lastName} (${student.enrollmentNumber})")
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text("No students available")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                coroutineScope.launch {
                    viewModel.addStudentsToCourse(courseId, token, selectedStudentsToAdd)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        modifier = Modifier.wrapContentWidth()
    )
}

@Composable
fun RemoveStudentsDialog(
    onDismissRequest: () -> Unit,
    studentsInCourse: List<Student>,
    selectedStudentsToRemove: MutableList<Student>,
    selectedStudentsToAdd: MutableList<Student>,
    onDeleteStudent: (Student) -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Remove Students") },
        text = {
            if (studentsInCourse.isEmpty()) {
                Text("No students currently assigned to remove.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(studentsInCourse) { student ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedStudentsToRemove.contains(student),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        selectedStudentsToRemove.add(student)
                                        selectedStudentsToAdd.remove(student)
                                    } else {
                                        selectedStudentsToRemove.remove(student)
                                    }
                                }
                            )
                            Text("${student.firstName} ${student.lastName} (${student.enrollmentNumber})")
                            Spacer(modifier = Modifier.width(8.dp))
                            DeleteButton(onDelete = { onDeleteStudent(student) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Remove")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        modifier = Modifier.wrapContentWidth()
    )
}

@Composable
fun DeleteButton(onDelete: () -> Unit) {
    Button(
        onClick = onDelete,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text("Delete")
    }
}