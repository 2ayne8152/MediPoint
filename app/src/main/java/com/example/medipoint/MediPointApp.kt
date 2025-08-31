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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medipoint.ui.theme.Screens.BookingScreen
import com.example.medipoint.ui.theme.Screens.AppointmentDetailScreen
import com.example.medipoint.ui.theme.Screens.HomeScreen
import com.example.medipoint.ui.theme.Screens.LoginScreen
import com.example.medipoint.ui.theme.Screens.ProfileScreen
import com.example.medipoint.ui.theme.Screens.RegistrationScreen
import com.example.medipoint.ui.theme.Viewmodels.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState

enum class MedipointScreens {
    HomeScreen,
    BookingScreen,
    AppointmentDetailScreen,
    ProfileScreen,
    LoginScreen,
    RegistrationScreen
}

@Composable
fun MediPointApp() {
    var currentScreen by remember { mutableStateOf(MedipointScreens.LoginScreen) }
    var isLoggedIn by remember { mutableStateOf(Firebase.auth.currentUser != null) }
    val authViewModel: AuthViewModel = viewModel()

    // If user is logged in, show main app, otherwise show auth screens
    if (isLoggedIn) {
        MainAppContent(
            onSignOut = {
                Firebase.auth.signOut()
                isLoggedIn = false
                currentScreen = MedipointScreens.LoginScreen
            }
        )
    } else {
        when (currentScreen) {
            MedipointScreens.LoginScreen -> {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { isLoggedIn = true },
                    onNavigateToRegistration = { currentScreen = MedipointScreens.RegistrationScreen }
                )
            }
            MedipointScreens.RegistrationScreen -> {
                RegistrationScreen(
                    authViewModel = authViewModel,
                    onRegistrationSuccess = {
                        // After successful registration, go back to login
                        currentScreen = MedipointScreens.LoginScreen
                    },
                    onBackToLogin = { currentScreen = MedipointScreens.LoginScreen }
                )
            }
            else -> {
                // Fallback to login screen
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { isLoggedIn = true },
                    onNavigateToRegistration = { currentScreen = MedipointScreens.RegistrationScreen }
                )
            }
        }
    }
}

@Composable
fun MainAppContent(onSignOut: () -> Unit) {
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
                HomeScreen(
                    onBookAppointmentClick = {navController.navigate(MedipointScreens.BookingScreen.name)},
                    onDetailClick = {navController.navigate(MedipointScreens.AppointmentDetailScreen.name)}
                )
            }
            composable(route = MedipointScreens.BookingScreen.name) {
                BookingScreen()
            }
            composable(route = MedipointScreens.AppointmentDetailScreen.name) {
                AppointmentDetailScreen()
            }
            composable(route = MedipointScreens.ProfileScreen.name) {
                ProfileScreen(onSignOut = onSignOut)
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
