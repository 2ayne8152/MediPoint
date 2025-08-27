@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.medipoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.medipoint.ui.screens.BookingScreen
import com.example.medipoint.ui.theme.MediPointTheme
import com.example.medipoint.ui.theme.Screens.AppointmentDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediPointTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BookingScreen()
                }
            }
        }
    }
}

@Composable
fun MediPointApp(modifier: Modifier = Modifier) {
    AppointmentDetailScreen()
}

@Composable
fun BottomNavigationBar() {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = { /* Navigate Home */ },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navigate Book */ },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Book") },
            label = { Text("Book") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navigate Alerts */ },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
            label = { Text("Alerts") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navigate Profile */ },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
