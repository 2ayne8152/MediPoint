package com.example.medipoint.ui.theme.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.Repository.AlertsRepository
import com.example.medipoint.Viewmodels.AlertViewModel
import com.example.medipoint.Viewmodels.AlertViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AlertsScreen(
    onDetailClick: (String) -> Unit = {} // Callback for "View Details"
) {
    val repository = AlertsRepository() // Create the repository instance
    val factory = AlertViewModelFactory(repository) // Create the ViewModelFactory

    // Use viewModel() with the factory to get the ViewModel instance
    val alertViewModel: AlertViewModel = viewModel(factory = factory)

    val alerts = alertViewModel.alerts.collectAsState() // Collect alerts as state
    val error = alertViewModel.error.collectAsState() // Collect error state

    // Fetch alerts each time the screen is recomposed
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "TEST_USER"
        alertViewModel.fetchAlerts(userId) // Fetch alerts from Firestore
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Alerts", style = MaterialTheme.typography.titleLarge, color = ComposeColor.Black)

        Spacer(modifier = Modifier.height(16.dp))

        // Show error message if any
        if (error.value != null) {
            Text(
                text = error.value ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = ComposeColor.Red
            )
        }
        // Loading state
        else if (alerts.value.isEmpty()) {
            Text(
                text = "There are no alerts at the moment.",
                style = MaterialTheme.typography.bodyMedium,
                color = ComposeColor.Gray
            )
        }
        // Display alerts if available
        else {
            alerts.value.forEach { alert ->
                AlertCard(
                    title = alert.title,
                    message = alert.message,
                    date = alert.date,
                    isRead = alert.isRead, // Pass the isRead field
                    modifier = Modifier.padding(top = 8.dp),
                    onDetailClick = { onDetailClick(alert.id) }, // Only pass onDetailClick now
                    onMarkAsRead = { alertViewModel.onMarkAsRead(alert.id) } // Pass onMarkAsRead to handle read status change
                )
            }
        }
    }
}


@Composable
fun AlertCard(
    title: String,
    message: String,
    date: String,
    isRead: Boolean, // Pass the isRead status here
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit,
    onMarkAsRead: () -> Unit // New parameter for "Mark as Read"
) {
    val textColor = if (isRead) ComposeColor.Gray else ComposeColor.Black // Darker for unread, lighter for read

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title and "Mark as Read" button in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = textColor) // Apply color to title
                // "Mark as Read" button
                IconButton(
                    onClick = onMarkAsRead,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark as Read",
                        tint = if (isRead) ComposeColor.Gray else ComposeColor.Blue // Change icon color based on read status
                    )
                }
            }

            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor, // Apply color to message
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                date,
                style = MaterialTheme.typography.bodySmall,
                color = textColor // Apply color to date
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = onDetailClick,
                    modifier = Modifier.fillMaxWidth(0.5f),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ComposeColor.Blue,  // Direct color usage
                        contentColor = ComposeColor.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(6.dp)
                ) {
                    Text("View Details")
                }
            }
        }
    }
}
