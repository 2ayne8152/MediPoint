// ----------------------------- MediPointApp.kt -----------------------------
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medipoint.Viewmodels.ThemeViewModel
import com.example.medipoint.Viewmodels.ThemeViewModelFactory
import com.example.medipoint.ui.theme.MediPointTheme
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
        fun appointmentDetail(appointmentId: String): String = "appointmentDetail/$appointmentId"
    }
}

@Composable
fun MediPointApp() {
    var currentScreen by remember { mutableStateOf(MedipointScreens.LoginScreen) }
    var isLoggedIn by remember { mutableStateOf(Firebase.auth.currentUser != null) }
    val authViewModel: AuthViewModel = viewModel()
    val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(LocalContext.current))
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    MediPointTheme(darkTheme = isDarkMode) {  // <-- Apply saved theme
        if (isLoggedIn) {
            MainAppContent(
                onSignOut = {
                    Firebase.auth.signOut()
                    isLoggedIn = false
                    currentScreen = MedipointScreens.LoginScreen
                },
                themeViewModel = themeViewModel
            )
        } else {
            when (currentScreen) {
                MedipointScreens.LoginScreen -> LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { isLoggedIn = true },
                    onNavigateToRegistration = { currentScreen = MedipointScreens.RegistrationScreen }
                )
                MedipointScreens.RegistrationScreen -> RegistrationScreen(
                    authViewModel = authViewModel,
                    onRegistrationSuccess = { currentScreen = MedipointScreens.LoginScreen },
                    onBackToLogin = { currentScreen = MedipointScreens.LoginScreen }
                )
                else -> LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { isLoggedIn = true },
                    onNavigateToRegistration = { currentScreen = MedipointScreens.RegistrationScreen }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    onSignOut: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

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
                                MedipointScreens.MedicalRecordsScreen.route -> "Medical Records"
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                AppointmentDetailScreen(appointmentId = appointmentId, navController = navController)
            }
            composable(MedipointScreens.ProfileScreen.route) {
                ProfileScreen(
                    onSignOut = onSignOut,
                    onNavigateToMedicalRecords = { navController.navigate(MedipointScreens.MedicalRecordsScreen.route) },
                    onNavigateToSettings = { navController.navigate(MedipointScreens.SettingsScreen.route) }
                )
            }
            composable(MedipointScreens.MedicalRecordsScreen.route) { MedicalRecordScreen(viewModel()) }
            composable(MedipointScreens.AllAppointmentsScreen.route) {
                AllAppointmentsScreen(
                    onDetailClick = { appointmentId -> navController.navigate(MedipointScreens.appointmentDetail(appointmentId)) }
                )
            }
            composable(MedipointScreens.SettingsScreen.route) {
                SettingsScreen(
                    isDarkMode = isDarkMode,
                    onToggleTheme = { newValue -> themeViewModel.toggleTheme(newValue) }
                )
            }
            composable(MedipointScreens.AlertsScreen.route) {
                AlertsScreen(
                    onDetailClick = { appointmentId -> navController.navigate(MedipointScreens.appointmentDetail(appointmentId)) }
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
            selected = currentRoute == MedipointScreens.AlertsScreen.route,
            onClick = { navController.navigate(MedipointScreens.AlertsScreen.route) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
            label = { Text("Alerts") }
        )
        NavigationBarItem(
            selected = currentRoute == MedipointScreens.ProfileScreen.route,
            onClick = { navController.navigate(MedipointScreens.ProfileScreen.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
