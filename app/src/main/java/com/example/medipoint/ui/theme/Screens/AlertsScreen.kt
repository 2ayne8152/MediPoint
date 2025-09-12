package com.example.medipoint.ui.theme.Screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.Viewmodels.AlertViewModel
import com.example.medipoint.Viewmodels.AlertViewModelFactory
import com.example.medipoint.Repository.AlertsRepository
import com.example.medipoint.Data.MediPointDatabase
import com.google.firebase.auth.FirebaseAuth

@Preview(showBackground = true)
@Composable
fun AlertsScreen(
    onDetailClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = AlertsRepository(context)

    val factory = AlertViewModelFactory(repository)
    val alertViewModel: AlertViewModel = viewModel(factory = factory)

    val alerts = alertViewModel.alerts.collectAsState()
    val error = alertViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "TEST_USER"
        alertViewModel.fetchAlerts(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Alerts", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        when {
            error.value != null -> {
                Text(
                    text = error.value ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ComposeColor.Red
                )
            }

            alerts.value.isEmpty() -> {
                Text(
                    "No alerts at the moment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ComposeColor.Gray
                )
            }

            else -> {
                alerts.value.forEach { alert ->
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
