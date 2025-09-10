package com.example.medipoint.ui.theme.Screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medipoint.Data.Appointment
import com.example.medipoint.Viewmodels.BookingViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@Preview (showBackground = true)
@Composable
fun AllAppointmentsScreen(
    bookingViewModel: BookingViewModel = viewModel(),
    onDetailClick: (String) -> Unit = {}
) {
    val appointments by bookingViewModel.appointments.collectAsState()

    // Filter by status
    val upcoming = appointments.filter { it.status == "Scheduled" }
    val previous = appointments.filter { it.status == "Completed" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Upcoming Section
        Text("Upcoming Appointments", style = MaterialTheme.typography.titleMedium)
        if (upcoming.isEmpty()) {
            Text(
                "No upcoming appointments.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            upcoming.forEach { appt ->
                AppointmentCard(
                    doctor = appt.doctorName,
                    specialty = appt.appointmentType,
                    date = appt.date,
                    time = appt.time,
                    modifier = Modifier.padding(top = 8.dp),
                    onDetailClick = { onDetailClick(appt.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Previous Section
        Text("Previous Appointments", style = MaterialTheme.typography.titleMedium)
        if (previous.isEmpty()) {
            Text(
                "No previous appointments.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            previous.forEach { appt ->
                AppointmentCard(
                    doctor = appt.doctorName,
                    specialty = appt.appointmentType,
                    date = appt.date,
                    time = appt.time,
                    modifier = Modifier.padding(top = 8.dp),
                    onDetailClick = { onDetailClick(appt.id) }
                )
            }
        }
    }
}


/**
 * Splits appointments into upcoming vs previous
 */
private fun splitAppointments(appointments: List<Appointment>): Pair<List<Appointment>, List<Appointment>> {
    val sdf = SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault())
    val now = System.currentTimeMillis()

    val upcoming = mutableListOf<Appointment>()
    val previous = mutableListOf<Appointment>()

    for (appt in appointments) {
        val millis = try {
            sdf.parse("${appt.date} ${appt.time}")?.time ?: 0L
        } catch (e: Exception) {
            0L
        }

        if (millis >= now) {
            upcoming.add(appt)
        } else {
            previous.add(appt)
        }
    }

    return Pair(upcoming, previous)
}

