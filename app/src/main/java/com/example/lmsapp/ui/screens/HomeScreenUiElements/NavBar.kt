package com.example.lmsapp.ui.screens.HomeScreenUiElements

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lmsapp.R

@Composable
fun NavBar(navController: NavController, role: String, username: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Log.d("NavDebug", "NavBar NavController hash: ${navController.hashCode()}")
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Home item
            NavBarItem(
                title = "Home",
                icon = painterResource(R.drawable.baseline_home_24)
            ) {
                try {
                    val currentRoute = navController.currentDestination?.route
                    val targetRoute = "home/$role/$username"

                    // Ensure we're not on the "Home" screen already
                    if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo("home/$role/$username") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }

                    }
                } catch (e: Exception) {
                    Log.e("Navigation Error", "Error navigating to 'home': ${e.message}")
                }
            }

            // Courses item
            NavBarItem(
                title = "Courses",
                icon = painterResource(R.drawable.baseline_menu_book_24)
            ) {
                try {
                    // Prevent navigating to Courses if already there
                    val currentRoute = navController.currentDestination?.route
                    val targetRoute = "courses/$role/$username"

                    if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo("home/$role/$username") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Navigation Error", "Error navigating to 'courses': ${e.message}")
                }
            }

            // Quiz item
            NavBarItem(
                title = "Quiz",
                icon = painterResource(R.drawable.baseline_quiz_24)
            ) {
                try {
                    val currentRoute = navController.currentDestination?.route
                    val targetRoute = "quiz"

                    // Prevent navigating to Quiz if already there
                    if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo("home/$role/$username") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }

                    }
                } catch (e: Exception) {
                    Log.e("Navigation Error", "Error navigating to 'quiz': ${e.message}")
                }
            }


            // Settings item
            NavBarItem(
                title = "Settings",
                icon = painterResource(R.drawable.baseline_settings_24)
            ) {
                try {
                    val currentRoute = navController.currentDestination?.route
                    val targetRoute = "settings/$role/$username"

                    if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo("home/$role/$username") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }

                    }
                } catch (e: Exception) {
                    Log.e("Navigation Error", "Error navigating to 'settings': ${e.message}")
                }
            }
        }
    }
}







@Composable
fun NavBarItem(title: String, icon: Painter, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            text = title,
            style = TextStyle(
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavBarPreview() {
    val dummyNavController = rememberNavController() // Proper preview-safe NavController
    NavBar(navController = dummyNavController, role = "teacher", username = "John Doe")
}
