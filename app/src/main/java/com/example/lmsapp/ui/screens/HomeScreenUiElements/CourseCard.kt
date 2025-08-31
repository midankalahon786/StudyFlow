package com.example.lmsapp.ui.screens.HomeScreenUiElements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

@Composable
fun CourseCard(
    title: String,
    instructor: String,
    imageUrl: String,
    progress: Float, // 0 to 1
    description: String
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp)) // Rounded corners for the card
            .background(MaterialTheme.colorScheme.surface) // Background color from theme
            .fillMaxWidth()
            //.height(200.dp)  //removed fixed height, the image defines the height.
            .padding(bottom = 8.dp),

        ) {
        // Course Image using Coil
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Course Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Fixed height for the image
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)), // Rounded corners for top
            contentScale = ContentScale.Crop, // Scale the image to fill the bounds
        )

        // Course Information
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface // Text color from theme
                ),
                maxLines = 2
            )
            Text(
                text = "by $instructor",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Secondary text color
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 2
            )

            // Progress Bar
            CourseProgressBar(progress = progress)
        }
    }
}

@Composable
fun CourseProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.outline) // Background of the progress bar
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress) // Use the progress value here
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary) // Color of the progress
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CourseCardPreview() {
    CourseCard(
        title = "Introduction to Android Development with Kotlin",
        instructor = "John Doe",
        imageUrl = "https://res.cloudinary.com/dfqucwsms/image/upload/v1745994747/image_dojwhk.webp", // Replace with a real image URL
        progress = 0.75f,
        description = "Learn the fundamentals of Android development and build your first app."
    )
}
