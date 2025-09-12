@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.example.medipoint

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medipoint.ui.theme.Screens.AlertsScreen
import com.example.medipoint.ui.theme.Screens.AllAppointmentsScreen
import com.example.medipoint.ui.theme.Screens.AppointmentDetailScreen
import com.example.medipoint.ui.theme.Screens.BookingScreen
import com.example.medipoint.ui.theme.Screens.HomeScreen
import com.example.medipoint.ui.theme.Screens.LoginScreen
import com.example.medipoint.ui.theme.Screens.MedicalRecordScreen
import com.example.medipoint.ui.theme.Screens.ProfileScreen
import com.example.medipoint.ui.theme.Screens.RegistrationScreen
import com.example.medipoint.ui.theme.Screens.SettingsScreen
import com.example.medipoint.ui.theme.Viewmodels.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

enum class MedipointScreens(val route: String) {
    HomeScreen("home"),
    BookingScreen("booking"),
    AppointmentDetailScreen("appointmentDetail/{appointmentId}"),
    ProfileScreen("profile"),
    LoginScreen("login"),
    RegistrationScreen("registration"),
    MedicalRecordsScreen("medicalRecords"),
    AllAppointmentsScreen("allAppointments"),
    SettingsScreen("settings"),
    AlertsScreen("alerts");

    companion object {
        fun appointmentDetail(appointmentId: String): String {
            return "appointmentDetail/$appointmentId"
        }
    }
}

@Composable
fun MediPointApp() {
    var currentScreen by remember { mutableStateOf(MedipointScreens.LoginScreen) }
    var isLoggedIn by remember { mutableStateOf(Firebase.auth.currentUser != null) }
    val authViewModel: AuthViewModel = viewModel()
    var isDarkMode by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        MainAppContent(
            onSignOut = {
                Firebase.auth.signOut()
                isLoggedIn = false
                currentScreen = MedipointScreens.LoginScreen
            },
            isDarkMode = isDarkMode,
            onToggleDarkMode = { isDarkMode = it }
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
                    onRegistrationSuccess = { currentScreen = MedipointScreens.LoginScreen },
                    onBackToLogin = { currentScreen = MedipointScreens.LoginScreen }
                )
            }
            else -> {
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
fun MainAppContent(
    onSignOut: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBackButton = currentRoute == MedipointScreens.AppointmentDetailScreen.route ||
            currentRoute == MedipointScreens.AllAppointmentsScreen.route ||
            currentRoute == MedipointScreens.SettingsScreen.route ||
            currentRoute == MedipointScreens.MedicalRecordsScreen.route

    Scaffold(
        topBar = {
            if (showBackButton) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                MedipointScreens.AppointmentDetailScreen.route -> "Appointment Details"
                                MedipointScreens.AllAppointmentsScreen.route -> "All Appointments"
                                MedipointScreens.SettingsScreen.route -> "Settings"
                                MedipointScreens.MedicalRecordsScreen.route -> "Medical records"
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0A0A1A),
                        titleContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MedipointScreens.HomeScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MedipointScreens.HomeScreen.route) {
                HomeScreen(
                    onBookAppointmentClick = { navController.navigate(MedipointScreens.BookingScreen.route) },
                    onDetailClick = { appointmentId -> navController.navigate(MedipointScreens.appointmentDetail(appointmentId)) },
                    onViewAllClick = { navController.navigate(MedipointScreens.AllAppointmentsScreen.route) }
                )
            }
            composable(MedipointScreens.BookingScreen.route) { BookingScreen() }
            composable(
                MedipointScreens.AppointmentDetailScreen.route,
                arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                AppointmentDetailScreen(
                    appointmentId = appointmentId,
                    navController = navController
                )
            }
            composable(MedipointScreens.ProfileScreen.route) {
                ProfileScreen(
                    onSignOut = onSignOut,
                    onNavigateToMedicalRecords = { navController.navigate(MedipointScreens.MedicalRecordsScreen.route) },
                    onNavigateToSettings = { navController.navigate(MedipointScreens.SettingsScreen.route) }
                )
            }
            composable(MedipointScreens.MedicalRecordsScreen.route) {
                MedicalRecordScreen(viewModel())
            }
            composable(MedipointScreens.AllAppointmentsScreen.route) {
                AllAppointmentsScreen(
                    onDetailClick = { appointmentId -> navController.navigate(MedipointScreens.appointmentDetail(appointmentId)) }
                )
            }
            composable(MedipointScreens.SettingsScreen.route) {
                SettingsScreen(
                    isDarkMode = remember { mutableStateOf(isDarkMode) },
                    onToggleTheme = onToggleDarkMode
                )
            }
            composable(MedipointScreens.AlertsScreen.route) {
                AlertsScreen(

                )
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
            selected = currentRoute == MedipointScreens.HomeScreen.route,
            onClick = { navController.navigate(MedipointScreens.HomeScreen.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == MedipointScreens.BookingScreen.route,
            onClick = { navController.navigate(MedipointScreens.BookingScreen.route) },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Book") },
            label = { Text("Book") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {navController.navigate(MedipointScreens.AlertsScreen.route)  },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
            label = { Text("Alerts") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(MedipointScreens.ProfileScreen.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )

    }
}
