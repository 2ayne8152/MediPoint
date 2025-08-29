@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.example.medipoint

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medipoint.ui.screens.BookingScreen
import com.example.medipoint.ui.theme.Screens.AppointmentDetailScreen
import com.example.medipoint.ui.theme.Screens.HomeScreen
import com.example.medipoint.ui.theme.Screens.ProfileScreen

enum class MedipointScreens {
    HomeScreen,
    BookingScreen,
    AppointmentDetailScreen,
    ProfileScreen
}


@Composable
fun MediPointApp() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Appointment Details", style = MaterialTheme.typography.titleMedium)
                        Text("Dr. Johnson", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A1A),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MedipointScreens.HomeScreen.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = MedipointScreens.HomeScreen.name) {
                HomeScreen( onBookAppointmentClick = {navController.navigate(MedipointScreens.BookingScreen.name)},
                    onDetailClick = {navController.navigate(MedipointScreens.AppointmentDetailScreen.name)})
            }
            composable(route = MedipointScreens.BookingScreen.name) {
                BookingScreen()
            }
            composable(route = MedipointScreens.AppointmentDetailScreen.name) {
                AppointmentDetailScreen()
            }
            composable (route = MedipointScreens.ProfileScreen.name) {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == MedipointScreens.HomeScreen.name,
            onClick = { navController.navigate(MedipointScreens.HomeScreen.name) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == MedipointScreens.BookingScreen.name,
            onClick = { navController.navigate(MedipointScreens.BookingScreen.name) },
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
            onClick = { navController.navigate(MedipointScreens.ProfileScreen.name) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
