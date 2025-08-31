package com.example.lmsapp.ui.screens

import CourseViewModel
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.lmsapp.ui.data.DataClasses.Resource
import com.example.lmsapp.ui.data.DataClasses.UIState

@Composable
fun StudentCourseResourcesScreen(viewModel: CourseViewModel) {
    val resourceState by viewModel.courseResources.collectAsState()

    when (resourceState) {
        is UIState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UIState.Success<*> -> {
            val resources = (resourceState as UIState.Success<List<Resource>>).data
            if (resources.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No resources available for this course.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(resources) { resource ->
                        StudentResourceCard(resource = resource)
                    }
                }
            }
        }

        is UIState.Error -> {
            val message = (resourceState as UIState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Failed to load resources: $message", color = MaterialTheme.colorScheme.error)
            }
        }

        is UIState.Empty -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data available.")
            }
        }

    }
}

@Composable
fun StudentResourceCard(resource: Resource) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = resource.title, style = MaterialTheme.typography.titleMedium)

            resource.description?.let {
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            resource.friendlyType?.let {
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(text = "Type: $it", style = MaterialTheme.typography.labelSmall)
            }

            resource.uploadDate?.let {
                Spacer(modifier = Modifier.padding(top = 2.dp))
                Text(text = "Uploaded: $it", style = MaterialTheme.typography.labelSmall)
            }

            resource.teacherName?.let {
                Spacer(modifier = Modifier.padding(top = 2.dp))
                Text(text = "By: $it", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            Log.d(
                "ResourceDebug",
                "Received resource type: ${resource.type}, friendlyType: ${resource.friendlyType}"
            )

            when (resource.friendlyType) {
                "DOCUMENT", "IMAGE", "VIDEO", "PRESENTATION", "SPREADSHEET" -> {
                    resource.fileName?.let { fileName ->
                        val fileUrl = "http://10.0.2.2:5008/api/uploads/resources/${resource.courseId}/${resource.fileName}"
                        val displayName = resource.originalName ?: fileName

                        Log.d("FileURL_Debug", "Generated fileUrl: $fileUrl for resourceId: ${resource.resourceId}")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, fileUrl.toUri())
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Preview")
                            }

                            Button(
                                onClick = {
                                    downloadFile(context, fileUrl, displayName)
                                }
                            ) {
                                Text("Download")
                            }
                        }
                    }
                }

                "LINK" -> {
                    resource.url?.let { link ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, link.toUri())
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Open Link")
                            }
                        }
                    }
                }

                "NOTE" -> {
                    resource.content?.let {
                        Text(text = "Note:", style = MaterialTheme.typography.labelMedium)
                        Text(text = it, style = MaterialTheme.typography.bodySmall)
                    }
                }

                else -> {
                    Text(
                        text = "No preview available for this resource type.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }
    }
}


fun downloadFile(context: Context, url: String, filename: String) {
    val request = DownloadManager.Request(url.toUri()).apply {
        setTitle("Downloading $filename")
        setDescription("Resource from LMS")
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        setAllowedOverMetered(true)
        setAllowedOverRoaming(true)
    }

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
}



