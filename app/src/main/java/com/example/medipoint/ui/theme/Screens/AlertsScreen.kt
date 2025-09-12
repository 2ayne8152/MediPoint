package com.example.medipoint.ui.theme.Screens

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.Repository.AlertsRepository
import com.example.medipoint.Viewmodels.AlertViewModel
import com.example.medipoint.Viewmodels.AlertViewModelFactory
import com.example.medipoint.Viewmodels.CheckInViewModel
import com.example.medipoint.Viewmodels.DistanceBannerUiState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.material3.Banner // This is the one you want
import com.google.accompanist.permissions.shouldShowRationale



@OptIn(ExperimentalPermissionsApi::class) // For Accompanist Permissions
@Composable
fun AlertsScreen(
    onDetailClick: (String) -> Unit = {},
    // Obtain ViewModels. Make sure you have a ViewModel factory for CheckInViewModel if it needs args
    // other than Application, or use hiltViewModel() if using Hilt.
    alertViewModel: AlertViewModel = viewModel(factory = AlertViewModelFactory(AlertsRepository())),
    checkInViewModel: CheckInViewModel = viewModel() // Assumes CheckInViewModel has a default factory
) {
    val alerts by alertViewModel.alerts.collectAsState()
    val error by alertViewModel.error.collectAsState()
    val distanceBannerState by checkInViewModel.distanceBannerState.collectAsState()

    val context = LocalContext.current

    // For location permission
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION // Correct: Directly access the constant
    )
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    // This will hold the appointment ID for which permission was requested,
    // so we can call the check after permission is granted.
    var pendingAppointmentIdForDistanceCheck by remember { mutableStateOf<String?>(null) }
    val CANCELLATION_RADIUS_METERS = 500.0 // Define your cancellation radius

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "TEST_USER" // Or handle appropriately
        alertViewModel.fetchAlerts(userId)
        checkInViewModel.prepareDistanceCheckBannerForNextAppointment() // Load banner state
    }

    // Effect to perform distance check after permission is granted
    LaunchedEffect(locationPermissionState.status, pendingAppointmentIdForDistanceCheck) {
        if (locationPermissionState.status.isGranted && pendingAppointmentIdForDistanceCheck != null) {
            checkInViewModel.performDistanceCheckAndCancelIfNeeded(
                appointmentId = pendingAppointmentIdForDistanceCheck!!,
                cancellationRadiusMeters = CANCELLATION_RADIUS_METERS
            )
            pendingAppointmentIdForDistanceCheck = null // Reset after use
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            // .padding(16.dp) // Padding will be applied to sections instead for banner to be full width
            .verticalScroll(rememberScrollState())
    ) {
        // --- Distance Check Banner ---
        val currentBanner = distanceBannerState
        if (currentBanner is DistanceBannerUiState.ShowBanner) {
            Banner( // Material 3 Banner
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    Icon(
                        Icons.Filled.Info, // Change as appropriate
                        contentDescription = "Information",
                        tint = if (currentBanner.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                },
                content = {
                    Text(
                        text = currentBanner.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentBanner.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    currentBanner.actionText?.let { actionText ->
                        TextButton(
                            onClick = {
                                if (actionText == "Check Distance") {
                                    pendingAppointmentIdForDistanceCheck = currentBanner.appointmentId
                                    if (locationPermissionState.status.isGranted) {
                                        // Permission already granted, directly perform check
                                        // The LaunchedEffect above will pick this up
                                    } else if (locationPermissionState.status.shouldShowRationale) {
                                        showPermissionRationaleDialog = true
                                    } else {
                                        locationPermissionState.launchPermissionRequest()
                                    }
                                }
                                // Handle other action texts if any
                            },
                            enabled = !currentBanner.isLoadingAction
                        ) {
                            Text(if (currentBanner.isLoadingAction) "Checking..." else actionText)
                        }
                    }
                    // Optional: Add a dismiss button if needed for non-critical banners
                    if (currentBanner.actionText == null && !currentBanner.isLoadingAction) {
                        TextButton(onClick = {
                            // Optionally, tell ViewModel to hide this specific banner or re-evaluate
                            // checkInViewModel.dismissBanner() // You'd need to implement this
                        }) {
                            Text("Dismiss")
                        }
                    }
                }
            )
            HorizontalDivider() // Separator after banner
        }

        // --- Original Alerts Content ---
        Column(modifier = Modifier.padding(16.dp)) { // Add padding for the alerts list
            Text("Alerts", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            when {
                error != null && error!!.isNotEmpty() -> { // Check if error is not null and not empty
                    Text(
                        text = error!!, // Directly use the error string
                        style = MaterialTheme.typography.bodyMedium,
                        color = ComposeColor.Red
                    )
                }

                alerts.isEmpty() -> {
                    Text(
                        "No alerts at the moment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ComposeColor.Gray
                    )
                }

                else -> {
                    alerts.forEach { alert ->
                        AlertCard(
                            title = alert.title,
                            message = alert.message,
                            date = alert.date,
                            modifier = Modifier.padding(top = 8.dp),
                            onDetailClick = { onDetailClick(alert.id) }
                        )
                    }
                }
            }
        }
    }

    // --- Permission Rationale Dialog (Example) ---
    if (showPermissionRationaleDialog) {
        AlertDialog( // You might want to style this better or use a custom dialog
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = { Text("Location Permission Needed") },
            text = { Text("This feature requires location access to check your distance from the hospital for your appointment. Please grant the permission.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationaleDialog = false
                    locationPermissionState.launchPermissionRequest()
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationaleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun AlertCard(
    title: String,
    message: String,
    date: String,
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = ComposeColor.Gray)
        Text(date, style = MaterialTheme.typography.bodySmall, color = ComposeColor.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onDetailClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Details")
        }
    }
}
